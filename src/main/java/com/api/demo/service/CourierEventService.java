package com.api.demo.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class CourierEventService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CourierQueueService courierQueueService;
    private final OrderAssignmentService orderAssignmentService;

    public CourierEventService(
            KafkaTemplate<String, Object> kafkaTemplate,
            CourierQueueService courierQueueService,
            OrderAssignmentService orderAssignmentService) {
        this.kafkaTemplate = kafkaTemplate;
        this.courierQueueService = courierQueueService;
        this.orderAssignmentService = orderAssignmentService;
    }

    /**
     * Kurye vardiya durumu değişikliklerini dinler
     */
    @KafkaListener(topics = "courier-status-changes", groupId = "courier-service")
    public void handleCourierStatusChange(Map<String, Object> statusChange) {
        Long courierId = Long.valueOf(statusChange.get("courierId").toString());
        String newStatus = statusChange.get("status").toString();

        switch (newStatus) {
            case "ONLINE":
                // Kurye vardiyaya girdi - sıraya ekle
                courierQueueService.addCourierToQueue(courierId);
                courierQueueService.updateCourierStatus(courierId, "AVAILABLE");

                // Bekleyen siparişleri kontrol et
                orderAssignmentService.processPendingOrdersForAvailableCourier(courierId);

                sendNotification("courier-events", "Courier " + courierId + " joined the queue");
                break;

            case "OFFLINE":
                // Kurye vardiyadan çıktı - sıradan çıkar
                courierQueueService.removeCourierFromQueue(courierId);
                sendNotification("courier-events", "Courier " + courierId + " left the queue");
                break;

            case "AVAILABLE":
                // Kurye müsait oldu - bekleyen siparişleri kontrol et
                courierQueueService.updateCourierStatus(courierId, "AVAILABLE");
                orderAssignmentService.processPendingOrdersForAvailableCourier(courierId);
                break;

            case "BUSY":
                // Kurye meşgul
                courierQueueService.updateCourierStatus(courierId, "BUSY");
                break;
        }
    }

    /**
     * Sipariş durumu değişikliklerini dinler
     */
    @KafkaListener(topics = "order-status-changes", groupId = "courier-service")
    public void handleOrderStatusChange(Map<String, Object> orderUpdate) {
        String orderStatus = orderUpdate.get("status").toString();
        Long orderId = Long.valueOf(orderUpdate.get("orderId").toString());
        Long courierId = orderUpdate.get("courierId") != null ?
            Long.valueOf(orderUpdate.get("courierId").toString()) : null;

        switch (orderStatus) {
            case "DELIVERED":
            case "CANCELLED":
                if (courierId != null) {
                    // Kurye tekrar müsait oldu
                    courierQueueService.updateCourierStatus(courierId, "AVAILABLE");

                    // Bekleyen siparişleri kontrol et
                    orderAssignmentService.processPendingOrdersForAvailableCourier(courierId);
                }
                break;

            case "PICKED_UP":
                if (courierId != null) {
                    courierQueueService.updateCourierStatus(courierId, "IN_DELIVERY");
                }
                break;
        }
    }

    /**
     * Lokasyon güncellemelerini dinler
     */
    @KafkaListener(topics = "courier-location-updates", groupId = "courier-service")
    public void handleLocationUpdate(Map<String, Object> locationUpdate) {
        Long courierId = Long.valueOf(locationUpdate.get("courierId").toString());
        Double latitude = Double.valueOf(locationUpdate.get("latitude").toString());
        Double longitude = Double.valueOf(locationUpdate.get("longitude").toString());

        // Redis'te lokasyonu güncelle
        courierQueueService.updateCourierLocation(courierId, latitude, longitude);

        // Yakındaki bekleyen siparişleri kontrol et ve bildirim gönder
        checkNearbyPendingOrders(courierId, latitude, longitude);
    }

    /**
     * Toplu optimizasyon isteklerini dinler
     */
    @KafkaListener(topics = "bulk-optimization", groupId = "courier-service")
    public void handleBulkOptimization(Map<String, Object> optimizationRequest) {
        // Toplu optimizasyon algoritmasını çalıştır
        int pendingCount = Integer.parseInt(optimizationRequest.get("pendingOrders").toString());
        int availableCount = Integer.parseInt(optimizationRequest.get("availableCouriers").toString());

        if (pendingCount > 10 && availableCount > 3) {
            // Gelişmiş optimizasyon algoritması
            performAdvancedOptimization();
        }
    }

    /**
     * Kurye vardiya başlangıcını tetikler
     */
    public void startCourierShift(Long courierId) {
        Map<String, Object> statusChange = new HashMap<>();
        statusChange.put("courierId", courierId);
        statusChange.put("status", "ONLINE");
        statusChange.put("timestamp", LocalDateTime.now().toString());
        statusChange.put("action", "SHIFT_START");

        kafkaTemplate.send("courier-status-changes", statusChange);
    }

    /**
     * Kurye vardiya bitişini tetikler
     */
    public void endCourierShift(Long courierId) {
        Map<String, Object> statusChange = new HashMap<>();
        statusChange.put("courierId", courierId);
        statusChange.put("status", "OFFLINE");
        statusChange.put("timestamp", LocalDateTime.now().toString());
        statusChange.put("action", "SHIFT_END");

        kafkaTemplate.send("courier-status-changes", statusChange);
    }

    /**
     * Kurye lokasyon güncellemesi gönderir
     */
    public void updateCourierLocation(Long courierId, double latitude, double longitude) {
        Map<String, Object> locationUpdate = new HashMap<>();
        locationUpdate.put("courierId", courierId);
        locationUpdate.put("latitude", latitude);
        locationUpdate.put("longitude", longitude);
        locationUpdate.put("timestamp", LocalDateTime.now().toString());

        kafkaTemplate.send("courier-location-updates", locationUpdate);
    }

    /**
     * Sipariş durumu güncellemesi gönderir
     */
    public void updateOrderStatus(Long orderId, Long courierId, String status) {
        Map<String, Object> orderUpdate = new HashMap<>();
        orderUpdate.put("orderId", orderId);
        orderUpdate.put("courierId", courierId);
        orderUpdate.put("status", status);
        orderUpdate.put("timestamp", LocalDateTime.now().toString());

        kafkaTemplate.send("order-status-changes", orderUpdate);
    }

    /**
     * Yeni sipariş bildirimi gönderir
     */
    public void notifyNewOrder(Long orderId, String businessName, String priority) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "NEW_ORDER");
        notification.put("orderId", orderId);
        notification.put("businessName", businessName);
        notification.put("priority", priority);
        notification.put("timestamp", LocalDateTime.now().toString());

        kafkaTemplate.send("new-orders", notification);

        // Otomatik atama için
        kafkaTemplate.send("auto-assignment-requests", notification);
    }

    // Yardımcı metodlar
    private void checkNearbyPendingOrders(Long courierId, double latitude, double longitude) {
        // Yakındaki bekleyen siparişleri kontrol et
        // Bu işlem için ayrı bir background service kullanılabilir
        Map<String, Object> proximityCheck = new HashMap<>();
        proximityCheck.put("courierId", courierId);
        proximityCheck.put("latitude", latitude);
        proximityCheck.put("longitude", longitude);
        proximityCheck.put("timestamp", LocalDateTime.now().toString());

        kafkaTemplate.send("proximity-checks", proximityCheck);
    }

    private void performAdvancedOptimization() {
        // Gelişmiş optimizasyon algoritması
        Map<String, Object> optimizationResult = new HashMap<>();
        optimizationResult.put("type", "ADVANCED_OPTIMIZATION");
        optimizationResult.put("timestamp", LocalDateTime.now().toString());
        optimizationResult.put("status", "STARTED");

        kafkaTemplate.send("optimization-results", optimizationResult);
    }

    private void sendNotification(String topic, String message) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("message", message);
        notification.put("timestamp", LocalDateTime.now().toString());

        kafkaTemplate.send(topic, notification);
    }
}
