package com.api.pako.service;

import com.api.pako.model.OrderAssignment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry userRegistry;

    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate,
                                        SimpUserRegistry userRegistry) {
        this.messagingTemplate = messagingTemplate;
        this.userRegistry = userRegistry;
    }

    /**
     * Kurye'ye yeni sipariş ataması bildirimi gönder
     */
    public void notifyNewAssignment(OrderAssignment assignment, Map<String, Object> orderDetails) {

        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "NEW_ASSIGNMENT");
        notification.put("assignmentId", assignment.getId());
        notification.put("orderId", assignment.getOrderId());
        notification.put("assignedAt", assignment.getAssignedAt());
        notification.put("timeoutAt", assignment.getTimeoutAt());
        notification.put("orderDetails", orderDetails);

        try {
            // Bağlı kullanıcıları logla - sadece DEBUG açıkken (user registry taraması pahalı)
            if (log.isDebugEnabled()) {
                var connectedUsers = userRegistry.getUsers().stream()
                        .map(u -> u.getName() + "(sessions=" + u.getSessions().size() + ")")
                        .collect(Collectors.joining(", "));
                log.debug("Connected WebSocket users: [{}], total={}", connectedUsers, userRegistry.getUserCount());
            }

            var targetUser = String.valueOf(assignment.getCourierId());
            log.debug("Sending notification to user '{}' at /queue/assignments", targetUser);

            messagingTemplate.convertAndSendToUser(
                    targetUser,
                "/queue/assignments",
                notification
            );
            log.debug("Sent new assignment notification to courier {}: assignment {}",
                       assignment.getCourierId(), assignment.getId());
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification to courier {}: {}",
                        assignment.getCourierId(), e.getMessage());
        }
    }

    /**
     * Business'e sipariş durumu bildirimi gönder
     */
    public void notifyBusinessOrderStatus(Long businessId, Long orderId, String status, String message) {

        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "ORDER_STATUS_UPDATE");
        notification.put("orderId", orderId);
        notification.put("status", status);
        notification.put("message", message);
        notification.put("timestamp", System.currentTimeMillis());

        try {
            messagingTemplate.convertAndSendToUser(
                String.valueOf(businessId),
                "/queue/orders",
                notification
            );
            log.info("Sent order status notification to business {}: order {} - {}",
                       businessId, orderId, status);
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification to business {}: {}",
                        businessId, e.getMessage());
        }
    }

    /**
     * Timeout bildirimi (atama iptal edildi, yeni kuryeye gidiyor)
     */
    public void notifyAssignmentTimeout(Long courierId, Long assignmentId) {

        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "ASSIGNMENT_TIMEOUT");
        notification.put("assignmentId", assignmentId);
        notification.put("message", "Atama zaman aşımına uğradı");

        try {
            messagingTemplate.convertAndSendToUser(
                String.valueOf(courierId),
                "/queue/assignments",
                notification
            );
            log.info("Sent timeout notification to courier {}: assignment {}",
                       courierId, assignmentId);
        } catch (Exception e) {
            log.error("Failed to send timeout notification: {}", e.getMessage());
        }
    }
}

