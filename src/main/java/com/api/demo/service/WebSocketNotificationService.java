package com.api.demo.service;

import com.api.demo.model.OrderAssignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WebSocketNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketNotificationService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Kurye'ye yeni sipariş ataması bildirimi gönder
     */
    public void notifyNewAssignment(OrderAssignment assignment, Map<String, Object> orderDetails) {
        String destination = "/queue/courier/" + assignment.getCourierId() + "/assignments";

        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "NEW_ASSIGNMENT");
        notification.put("assignmentId", assignment.getId());
        notification.put("orderId", assignment.getOrderId());
        notification.put("assignedAt", assignment.getAssignedAt());
        notification.put("timeoutAt", assignment.getTimeoutAt());
        notification.put("orderDetails", orderDetails);

        try {
            messagingTemplate.convertAndSendToUser(
                String.valueOf(assignment.getCourierId()),
                "/queue/assignments",
                notification
            );
            logger.info("Sent new assignment notification to courier {}: assignment {}",
                       assignment.getCourierId(), assignment.getId());
        } catch (Exception e) {
            logger.error("Failed to send WebSocket notification to courier {}: {}",
                        assignment.getCourierId(), e.getMessage());
        }
    }

    /**
     * Business'e sipariş durumu bildirimi gönder
     */
    public void notifyBusinessOrderStatus(Long businessId, Long orderId, String status, String message) {
        String destination = "/queue/business/" + businessId + "/orders";

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
            logger.info("Sent order status notification to business {}: order {} - {}",
                       businessId, orderId, status);
        } catch (Exception e) {
            logger.error("Failed to send WebSocket notification to business {}: {}",
                        businessId, e.getMessage());
        }
    }

    /**
     * Timeout bildirimi (atama iptal edildi, yeni kuryeye gidiyor)
     */
    public void notifyAssignmentTimeout(Long courierId, Long assignmentId) {
        String destination = "/queue/courier/" + courierId + "/assignments";

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
            logger.info("Sent timeout notification to courier {}: assignment {}",
                       courierId, assignmentId);
        } catch (Exception e) {
            logger.error("Failed to send timeout notification: {}", e.getMessage());
        }
    }
}

