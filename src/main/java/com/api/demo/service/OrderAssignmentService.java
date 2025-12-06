package com.api.demo.service;

import com.api.demo.exception.*;
import com.api.demo.model.Courier;
import com.api.demo.model.OnDutyCourier;
import com.api.demo.model.Order;
import com.api.demo.model.OrderAssignment;
import com.api.demo.model.enums.AssignmentStatus;
import com.api.demo.model.enums.AssignmentType;
import com.api.demo.model.enums.OrderStatus;
import com.api.demo.repository.CourierRepository;
import com.api.demo.repository.OrderAssignmentRepository;
import com.api.demo.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderAssignmentService {

    private static final Logger logger = LoggerFactory.getLogger(OrderAssignmentService.class);

    @Value("${order.assignment.timeout.minutes:2}")
    private int assignmentTimeoutMinutes;

    private final OnDutyService onDutyService;
    private final OrderRepository orderRepository;
    private final OrderAssignmentRepository orderAssignmentRepository;
    private final WebSocketNotificationService notificationService;
    private final CourierRepository courierRepository;

    public OrderAssignmentService(
            OnDutyService onDutyService,
            OrderRepository orderRepository,
            OrderAssignmentRepository orderAssignmentRepository,
            WebSocketNotificationService notificationService,
            CourierRepository courierRepository) {
        this.onDutyService = onDutyService;
        this.orderRepository = orderRepository;
        this.orderAssignmentRepository = orderAssignmentRepository;
        this.notificationService = notificationService;
        this.courierRepository = courierRepository;
    }

    /**
     * FIFO ile sıradaki kuryeye ata
     */
    @Transactional
    public OrderAssignment assignToNextAvailableCourier(Long orderId) {
        return assignToNextAvailableCourier(orderId, AssignmentType.AUTO);
    }

    @Transactional
    public OrderAssignment assignToNextAvailableCourier(Long orderId, AssignmentType assignmentType) {
        logger.info("Attempting to assign order {} with type {}", orderId, assignmentType);

        // DUPLICATE KONTROLÜ: Eğer order'ın zaten aktif PENDING assignment'ı varsa, yeni oluşturma!
        if (orderAssignmentRepository.existsByOrderIdAndStatus(orderId, AssignmentStatus.PENDING)) {
            logger.warn("Order {} already has a PENDING assignment, skipping duplicate creation", orderId);
            return orderAssignmentRepository.findByOrderIdAndStatus(orderId, AssignmentStatus.PENDING)
                    .orElseThrow(() -> new BusinessException("Beklenmeyen durum: Pending assignment bulunamadı"));
        }

        // Eğer REASSIGNMENT ise, son timeout olan kuryeyi kontrol et
        if (assignmentType == AssignmentType.REASSIGNMENT) {
            Long lastTimedOutCourierId = orderAssignmentRepository
                .findByOrderIdAndStatusOrderByAssignedAtDesc(orderId, AssignmentStatus.TIMEOUT)
                .stream()
                .findFirst()
                .map(OrderAssignment::getCourierId)
                .orElse(null);

            if (lastTimedOutCourierId != null) {
                long totalOnDutyCouriers = onDutyService.countOnDutyCouriers();
                if (totalOnDutyCouriers <= 1) {
                    logger.warn("Cannot reassign order {}: only 1 courier on-duty (courier {}). " +
                              "Order will remain unassigned until more couriers are available or current courier accepts.",
                              orderId, lastTimedOutCourierId);
                    throw new BusinessException("Şu anda yeterli kurye yok. Sipariş beklemede kalacak.");
                }
            }
        }

        // 1. FIFO'dan en eski on-duty kurye
        OnDutyCourier nextCourier;
        try {
            nextCourier = onDutyService.getNextInQueue();
        } catch (RuntimeException e) {
            logger.error("No courier available for order {}: {}", orderId, e.getMessage());
            throw new NoCourierAvailableException("Şu anda aktif kurye yok. Lütfen daha sonra tekrar deneyin.");
        }

        logger.info("Next courier in queue: {} (on_duty_since: {})",
                   nextCourier.getCourierId(), nextCourier.getOnDutySince());

        // 2. Order'ı güncelle
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Sipariş bulunamadı"));

        // Courier entity'yi çek
        Courier courier = courierRepository.findById(nextCourier.getCourierId())
                .orElseThrow(() -> new BusinessException("Kurye bulunamadı"));

        order.setStatus(OrderStatus.PENDING);
        order.setCourier(courier);
        orderRepository.save(order);

        // 3. Assignment kaydı oluştur
        OrderAssignment assignment = new OrderAssignment();
        assignment.setOrderId(orderId);
        assignment.setCourierId(nextCourier.getCourierId());
        assignment.setStatus(AssignmentStatus.PENDING);
        assignment.setAssignmentType(assignmentType);
        assignment.setAssignedAt(OffsetDateTime.now(ZoneOffset.UTC));
        assignment.setTimeoutAt(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(assignmentTimeoutMinutes));

        OrderAssignment savedAssignment = orderAssignmentRepository.save(assignment);
        logger.info("Created assignment {}: order {} → courier {} (timeout: {})",
                   savedAssignment.getId(), orderId, nextCourier.getCourierId(), savedAssignment.getTimeoutAt());

        // 4. WebSocket bildirimi gönder (hata olursa assignment yine de oluşturulsun)
        try {
            Map<String, Object> orderDetails = new HashMap<>();
            orderDetails.put("pickupAddress", order.getPickupAddress());
            orderDetails.put("deliveryAddress", order.getDeliveryAddress());
            orderDetails.put("packageDescription", order.getPackageDescription());
            orderDetails.put("deliveryFee", order.getDeliveryFee());
            orderDetails.put("endCustomerName", order.getEndCustomerName());

            notificationService.notifyNewAssignment(savedAssignment, orderDetails);
        } catch (Exception e) {
            logger.warn("Failed to send WebSocket notification for assignment {}: {}",
                       savedAssignment.getId(), e.getMessage());
            // Assignment başarıyla oluşturuldu, sadece notification başarısız - devam et
        }

        return savedAssignment;
    }

    /**
     * Kurye onayı
     */
    @Transactional
    public void acceptAssignment(Long assignmentId, Long courierId) {
        logger.info("Courier {} accepting assignment {}", courierId, assignmentId);

        OrderAssignment assignment = orderAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));

        if (!assignment.getCourierId().equals(courierId)) {
            throw new AssignmentNotOwnedException(assignmentId, courierId);
        }

        if (assignment.getStatus() != AssignmentStatus.PENDING) {
            throw new InvalidAssignmentStatusException(
                assignmentId,
                assignment.getStatus(),
                "Bu atama artık geçersiz (durum: " + assignment.getStatus() + ")"
            );
        }

        // Timeout kontrolü
        if (assignment.getTimeoutAt() != null &&
            OffsetDateTime.now(ZoneOffset.UTC).isAfter(assignment.getTimeoutAt())) {
            throw new AssignmentExpiredException(
                assignmentId,
                "Atama süresi doldu (4 dakikalık yanıt süresi aşıldı). Sipariş başka bir kuryeye atanmış olabilir."
            );
        }

        // Assignment'ı güncelle
        assignment.setStatus(AssignmentStatus.ACCEPTED);
        assignment.setResponseAt(OffsetDateTime.now(ZoneOffset.UTC));
        orderAssignmentRepository.save(assignment);

        // Order'ı ASSIGNED yap ve courier'i set et (kritik!)
        Order order = orderRepository.findById(assignment.getOrderId())
                .orElseThrow(() -> new BusinessException("Sipariş bulunamadı"));

        logger.debug("Before update - Order {}: status={}, courierId={}",
                    order.getId(), order.getStatus(),
                    order.getCourier() != null ? order.getCourier().getId() : "NULL");

        // Courier entity'yi getir ve order'a set et
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new BusinessException("Kurye bulunamadı"));

        order.setStatus(OrderStatus.ASSIGNED);
        order.setCourier(courier); // ← ÖNEMLİ: Courier'i order'a bağla!
        Order savedOrder = orderRepository.save(order);

        logger.info("After update - Order {}: status={}, courierId={}, courier set successfully",
                   savedOrder.getId(), savedOrder.getStatus(),
                   savedOrder.getCourier() != null ? savedOrder.getCourier().getId() : "NULL");

        // Kurye'yi kuyruğun sonuna taşı
        try {
            onDutyService.moveToEndOfQueue(courierId);
        } catch (Exception e) {
            logger.warn("Failed to move courier {} to end of queue: {}", courierId, e.getMessage());
            // Accept işlemi başarılı, queue pozisyonu güncellenemedi - önemli değil
        }

        logger.info("Assignment {} accepted by courier {}", assignmentId, courierId);

        // Business'e bildir (hata olursa logla ama accept'i başarısız sayma)
        try {
            if (order.getBusiness() != null) {
                notificationService.notifyBusinessOrderStatus(
                    order.getBusiness().getId(),
                    order.getId(),
                    "ASSIGNED",
                    "Sipariş kurye tarafından kabul edildi"
                );
            }
        } catch (Exception e) {
            logger.warn("Failed to notify business for order {}: {}", order.getId(), e.getMessage());
            // Accept başarılı, sadece notification başarısız
        }
    }

    /**
     * Kurye reddi
     */
    @Transactional
    public void rejectAssignment(Long assignmentId, Long courierId, String reason) {
        logger.info("Courier {} rejecting assignment {}: {}", courierId, assignmentId, reason);

        OrderAssignment assignment = orderAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));

        if (!assignment.getCourierId().equals(courierId)) {
            throw new AssignmentNotOwnedException(assignmentId, courierId);
        }

        if (assignment.getStatus() != AssignmentStatus.PENDING) {
            throw new InvalidAssignmentStatusException(
                assignmentId,
                assignment.getStatus(),
                "Bu atama artık geçersiz"
            );
        }

        // Assignment'ı güncelle
        assignment.setStatus(AssignmentStatus.REJECTED);
        assignment.setResponseAt(OffsetDateTime.now(ZoneOffset.UTC));
        assignment.setRejectionReason(reason);
        orderAssignmentRepository.save(assignment);

        logger.info("Assignment {} rejected, reassigning to next courier", assignmentId);

        // Bir sonraki kuryeye ata
        try {
            assignToNextAvailableCourier(assignment.getOrderId(), AssignmentType.REASSIGNMENT);
        } catch (BusinessException e) {
            logger.warn("Failed to reassign order {} after rejection: {}", assignment.getOrderId(), e.getMessage());
            // Reject başarılı, ama yeniden atama yapılamadı (örn: kurye yok)
            // Order PENDING kalacak, sonra manuel atanabilir veya yeni kurye geldiğinde atanır
            throw new BusinessException("Sipariş reddedildi ancak başka kurye yok. Order beklemede kalacak.");
        }
    }

    /**
     * Kurye'nin bekleyen atamalarını getir (timeout olanları hariç)
     */
    public List<OrderAssignment> getPendingAssignments(Long courierId) {
        // Database seviyesinde timeout kontrolü yapan optimized query kullan
        return orderAssignmentRepository.findValidPendingAssignmentsByCourierId(courierId);
    }

    /**
     * Timeout kontrolü - Her 30 saniyede çalışır
     */
    @Scheduled(fixedDelay = 30000) // 30 seconds
    @Transactional
    public void checkTimeouts() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        List<OrderAssignment> timedOutAssignments = orderAssignmentRepository
                .findByStatusAndTimeoutAtBefore(AssignmentStatus.PENDING, now);

        if (!timedOutAssignments.isEmpty()) {
            logger.info("Found {} timed out assignments", timedOutAssignments.size());
        }

        for (OrderAssignment assignment : timedOutAssignments) {
            try {
                logger.warn("Assignment {} timed out (courier: {}, order: {})",
                           assignment.getId(), assignment.getCourierId(), assignment.getOrderId());

                // Assignment'ı timeout yap
                assignment.setStatus(AssignmentStatus.TIMEOUT);
                assignment.setResponseAt(now);
                assignment.setRejectionReason("Yanıt süresi doldu (4 dakika içinde yanıt verilmedi)");
                orderAssignmentRepository.save(assignment);

                // Kurye'ye bildir
                notificationService.notifyAssignmentTimeout(
                    assignment.getCourierId(),
                    assignment.getId()
                );

                // Bir sonraki kuryeye ata
                try {
                    assignToNextAvailableCourier(assignment.getOrderId(), AssignmentType.REASSIGNMENT);
                } catch (BusinessException be) {
                    // Yeterli kurye yok - Order beklemede kalacak
                    logger.warn("Cannot reassign order {}: {}", assignment.getOrderId(), be.getMessage());
                    // Order status'ünü PENDING tut, kurye boşaldığında veya yeni kurye geldiğinde atanacak
                }

            } catch (Exception e) {
                logger.error("Error handling timeout for assignment {}: {}",
                           assignment.getId(), e.getMessage());
            }
        }
    }
}

