package com.api.pako.service;

import com.api.pako.model.OrderAssignment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSocketNotificationServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private SimpUserRegistry userRegistry;

    @InjectMocks
    private WebSocketNotificationService underTest;

    @Test
    void notifyNewAssignmentSendsToCourierQueue() {
        // GIVEN
        when(userRegistry.getUsers()).thenReturn(Collections.emptySet());

        var assignment = new OrderAssignment();
        assignment.setId(100L);
        assignment.setOrderId(5L);
        assignment.setCourierId(10L);
        assignment.setAssignedAt(OffsetDateTime.now(ZoneOffset.UTC));
        assignment.setTimeoutAt(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(4));

        var orderDetails = Map.<String, Object>of("pickupAddress", "Beşiktaş");

        // WHEN
        underTest.notifyNewAssignment(assignment, orderDetails);

        // THEN
        verify(messagingTemplate).convertAndSendToUser(
                eq("10"),
                eq("/queue/assignments"),
                assertArg(payload -> assertThat(asMap(payload))
                        .containsEntry("type", "NEW_ASSIGNMENT")
                        .containsEntry("assignmentId", 100L)
                        .containsEntry("orderId", 5L)
                        .containsEntry("orderDetails", orderDetails)));
    }

    @Test
    void notifyNewAssignmentSwallowsMessagingFailure() {
        // GIVEN
        when(userRegistry.getUsers()).thenReturn(Collections.emptySet());
        doThrow(new RuntimeException("broker down"))
                .when(messagingTemplate).convertAndSendToUser(any(), any(), any(Object.class));

        var assignment = new OrderAssignment();
        assignment.setId(1L);
        assignment.setCourierId(10L);

        // WHEN & THEN - exception must not propagate (assignment flow must continue)
        assertThatCode(() -> underTest.notifyNewAssignment(assignment, Map.of()))
                .doesNotThrowAnyException();
    }

    @Test
    void notifyBusinessOrderStatusSendsToBusinessQueue() {
        // WHEN
        underTest.notifyBusinessOrderStatus(7L, 5L, "DELIVERED", "Teslim edildi");

        // THEN
        verify(messagingTemplate).convertAndSendToUser(
                eq("7"),
                eq("/queue/orders"),
                assertArg(payload -> assertThat(asMap(payload))
                        .containsEntry("type", "ORDER_STATUS_UPDATE")
                        .containsEntry("orderId", 5L)
                        .containsEntry("status", "DELIVERED")
                        .containsEntry("message", "Teslim edildi")));
    }

    @Test
    void notifyAssignmentTimeoutSendsToCourierQueue() {
        // WHEN
        underTest.notifyAssignmentTimeout(10L, 100L);

        // THEN
        verify(messagingTemplate).convertAndSendToUser(
                eq("10"),
                eq("/queue/assignments"),
                assertArg(payload -> assertThat(asMap(payload))
                        .containsEntry("type", "ASSIGNMENT_TIMEOUT")
                        .containsEntry("assignmentId", 100L)));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object payload) {
        return (Map<String, Object>) payload;
    }
}
