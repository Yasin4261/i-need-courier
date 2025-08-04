package com.api.demo.service;

import com.api.demo.entity.Business;
import com.api.demo.entity.Courier;
import com.api.demo.entity.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderAssignmentService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CourierQueueService courierQueueService;
    private final CourierService courierService;

    public OrderAssignmentService(
            RedisTemplate<String, Object> redisTemplate,
            KafkaTemplate<String, Object> kafkaTemplate,
            CourierQueueService courierQueueService,
            CourierService courierService) {
        this.redisTemplate = redisTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.courierQueueService = courierQueueService;
        this.courierService = courierService;
    }

    private static final String PENDING_ORDERS_KEY = "orders:pending";
    private static final String COURIER_ORDERS_KEY = "courier:orders:";
    private static final String BUSINESS_ORDERS_KEY = "business:orders:";
    private static final double MAX_DISTANCE_KM = 10.0; // Maksimum atama mesafesi

    /**
     * Yeni sipariş geldiğinde otomatik atama yapar
     */
    public AssignmentResult assignOrderToCourier(Order order) {
        // Pending orders cache'e ekle
        addOrderToPendingQueue(order);

        // En uygun kuryeyi bul
        CourierMatch bestMatch = findOptimalCourier(order);

        if (bestMatch != null) {
            // Kuryeye ata
            assignOrderDirectly(order.getId(), bestMatch.getCourierId());

            // Kafka'ya bildirim gönder
            sendOrderAssignmentNotification(order, bestMatch.getCourierId());

            return new AssignmentResult(true, bestMatch.getCourierId(), "Order successfully assigned");
        } else {
            // Müsait kurye yok, beklemede kalacak
            return new AssignmentResult(false, null, "No available courier found, order queued");
        }
    }

    /**
     * En uygun kuryeyi bulur (mesafe + sıra + mevcut yük)
     */
    private CourierMatch findOptimalCourier(Order order) {
        List<Object> courierQueue = courierQueueService.getCourierQueue();
        if (courierQueue == null || courierQueue.isEmpty()) {
            return null;
        }

        List<CourierMatch> candidates = new ArrayList<>();

        for (Object courierIdObj : courierQueue) {
            Long courierId = Long.valueOf(courierIdObj.toString());

            if (!courierQueueService.isCourierAvailable(courierId)) {
                continue;
            }

            Optional<Courier> courierOpt = courierService.getCourierById(courierId)
                .map(dto -> convertDTOToEntity(dto));

            if (courierOpt.isEmpty()) {
                continue;
            }

            Courier courier = courierOpt.get();

            // Mesafe hesapla
            double distance = calculateDistance(
                order.getPickupLatitude(), order.getPickupLongitude(),
                courier.getCurrentLatitude(), courier.getCurrentLongitude()
            );

            if (distance <= MAX_DISTANCE_KM) {
                // Kurye yükünü kontrol et
                int currentLoad = getCourierCurrentLoad(courierId);
                int maxLoad = getMaxLoadForVehicle(courier.getVehicleType());

                if (currentLoad < maxLoad) {
                    candidates.add(new CourierMatch(
                        courierId,
                        distance,
                        currentLoad,
                        maxLoad,
                        getCourierPriority(courierId)
                    ));
                }
            }
        }

        // En uygun kuryeyi seç (mesafe + yük + sıra önceliği)
        return candidates.stream()
            .min(Comparator
                .comparingDouble(CourierMatch::getDistance)
                .thenComparingInt(CourierMatch::getCurrentLoad)
                .thenComparingInt(CourierMatch::getPriority))
            .orElse(null);
    }

    /**
     * Siparişi doğrudan kuryeye atar
     */
    public void assignOrderDirectly(Long orderId, Long courierId) {
        // Redis'te atamayı kaydet
        redisTemplate.opsForSet().add(COURIER_ORDERS_KEY + courierId, orderId);

        // Kurye durumunu güncelle
        courierQueueService.updateCourierStatus(courierId, "BUSY");

        // Pending orders'dan çıkar
        redisTemplate.opsForSet().remove(PENDING_ORDERS_KEY, orderId);

        // Cache'e atama bilgisini kaydet
        Map<String, Object> assignmentData = new HashMap<>();
        assignmentData.put("orderId", orderId);
        assignmentData.put("courierId", courierId);
        assignmentData.put("assignedAt", LocalDateTime.now().toString());

        redisTemplate.opsForValue().set(
            "assignment:" + orderId,
            assignmentData,
            java.time.Duration.ofHours(24)
        );
    }

    /**
     * Kurye müsait olduğunda bekleyen siparişleri kontrol eder
     */
    public void processPendingOrdersForAvailableCourier(Long courierId) {
        Set<Object> pendingOrders = redisTemplate.opsForSet().members(PENDING_ORDERS_KEY);

        if (pendingOrders == null || pendingOrders.isEmpty()) {
            return;
        }

        // En uygun bekleyen siparişi bul
        for (Object orderIdObj : pendingOrders) {
            Long orderId = Long.valueOf(orderIdObj.toString());

            // Order bilgilerini cache'den al (veya DB'den)
            Order order = getOrderFromCache(orderId);
            if (order != null) {
                CourierMatch match = evaluateCourierForOrder(courierId, order);
                if (match != null) {
                    assignOrderDirectly(orderId, courierId);
                    sendOrderAssignmentNotification(order, courierId);
                    break; // Bir sipariş atandı, döngüden çık
                }
            }
        }
    }

    /**
     * Toplu optimizasyon - çok sipariş varsa optimize eder
     */
    public void optimizeBulkAssignments() {
        Set<Object> pendingOrders = redisTemplate.opsForSet().members(PENDING_ORDERS_KEY);
        List<Object> availableCouriers = courierQueueService.getCourierQueue().stream()
            .filter(courierId -> courierQueueService.isCourierAvailable(Long.valueOf(courierId.toString())))
            .collect(Collectors.toList());

        if (pendingOrders == null || pendingOrders.size() < 5 || availableCouriers.size() < 2) {
            return; // Optimizasyon gerekmez
        }

        // Kafka'ya toplu optimizasyon mesajı gönder
        Map<String, Object> optimizationRequest = new HashMap<>();
        optimizationRequest.put("pendingOrders", pendingOrders.size());
        optimizationRequest.put("availableCouriers", availableCouriers.size());
        optimizationRequest.put("timestamp", LocalDateTime.now().toString());

        kafkaTemplate.send("bulk-optimization", optimizationRequest);
    }

    /**
     * Kafka notification gönderir
     */
    private void sendOrderAssignmentNotification(Order order, Long courierId) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "ORDER_ASSIGNED");
        notification.put("orderId", order.getId());
        notification.put("orderNumber", order.getOrderNumber());
        notification.put("courierId", courierId);
        notification.put("businessId", order.getBusiness().getId());
        notification.put("priority", order.getPriority().toString());
        notification.put("timestamp", LocalDateTime.now().toString());

        // Farklı topic'lere gönder
        kafkaTemplate.send("order-assignments", notification);
        kafkaTemplate.send("courier-notifications", notification);
    }

    // Yardımcı metodlar
    private void addOrderToPendingQueue(Order order) {
        redisTemplate.opsForSet().add(PENDING_ORDERS_KEY, order.getId());

        // Order cache'e kaydet
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("id", order.getId());
        orderData.put("businessId", order.getBusiness().getId());
        orderData.put("pickupLat", order.getPickupLatitude());
        orderData.put("pickupLng", order.getPickupLongitude());
        orderData.put("priority", order.getPriority().toString());

        redisTemplate.opsForValue().set(
            "order:cache:" + order.getId(),
            orderData,
            java.time.Duration.ofHours(6)
        );
    }

    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return Double.MAX_VALUE;
        }

        // Haversine formula
        double R = 6371; // Earth radius in kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    private int getCourierCurrentLoad(Long courierId) {
        Set<Object> orders = redisTemplate.opsForSet().members(COURIER_ORDERS_KEY + courierId);
        return orders != null ? orders.size() : 0;
    }

    private int getMaxLoadForVehicle(Courier.VehicleType vehicleType) {
        return switch (vehicleType) {
            case WALKING, BICYCLE -> 2;
            case MOTORCYCLE -> 4;
            case CAR -> 6;
            case VAN -> 10;
            case TRUCK -> 15;
            default -> 3;
        };
    }

    private int getCourierPriority(Long courierId) {
        // Sıradaki pozisyonu döndür (düşük sayı = yüksek öncelik)
        List<Object> queue = courierQueueService.getCourierQueue();
        return queue.indexOf(courierId);
    }

    private CourierMatch evaluateCourierForOrder(Long courierId, Order order) {
        // Kurye için order uygunluğunu değerlendir
        // Basitleştirilmiş versiyon
        return new CourierMatch(courierId, 1.0, 0, 5, 0);
    }

    private Order getOrderFromCache(Long orderId) {
        // Cache'den order bilgilerini al
        // Implementasyon basitleştirildi
        return null;
    }

    private Courier convertDTOToEntity(com.api.demo.dto.CourierDTO dto) {
        // DTO'dan Entity'ye çevir
        // Implementasyon basitleştirildi
        return new Courier();
    }

    // Inner classes
    public static class AssignmentResult {
        private boolean success;
        private Long courierId;
        private String message;

        public AssignmentResult(boolean success, Long courierId, String message) {
            this.success = success;
            this.courierId = courierId;
            this.message = message;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public Long getCourierId() { return courierId; }
        public String getMessage() { return message; }
    }

    public static class CourierMatch {
        private Long courierId;
        private double distance;
        private int currentLoad;
        private int maxLoad;
        private int priority;

        public CourierMatch(Long courierId, double distance, int currentLoad, int maxLoad, int priority) {
            this.courierId = courierId;
            this.distance = distance;
            this.currentLoad = currentLoad;
            this.maxLoad = maxLoad;
            this.priority = priority;
        }

        // Getters
        public Long getCourierId() { return courierId; }
        public double getDistance() { return distance; }
        public int getCurrentLoad() { return currentLoad; }
        public int getMaxLoad() { return maxLoad; }
        public int getPriority() { return priority; }
    }
}
