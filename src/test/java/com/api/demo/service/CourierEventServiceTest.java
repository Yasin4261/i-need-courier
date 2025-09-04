package com.api.demo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import java.util.HashMap;
import java.util.Map;
import static org.mockito.Mockito.*;

class CourierEventServiceTest {
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Mock
    private CourierQueueService courierQueueService;
    @Mock
    private OrderAssignmentService orderAssignmentService;
    @InjectMocks
    private CourierEventService courierEventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleCourierStatusChange_online_shouldAddToQueueAndProcessOrders() {
        Map<String, Object> statusChange = new HashMap<>();
        statusChange.put("courierId", 1L);
        statusChange.put("status", "ONLINE");

        courierEventService.handleCourierStatusChange(statusChange);

        verify(courierQueueService).addCourierToQueue(1L);
        verify(courierQueueService).updateCourierStatus(1L, "AVAILABLE");
        verify(orderAssignmentService).processPendingOrdersForAvailableCourier(1L);
        verify(kafkaTemplate).send(eq("courier-events"), anyString());
    }

    @Test
    void handleCourierStatusChange_offline_shouldRemoveFromQueue() {
        Map<String, Object> statusChange = new HashMap<>();
        statusChange.put("courierId", 2L);
        statusChange.put("status", "OFFLINE");

        courierEventService.handleCourierStatusChange(statusChange);

        verify(courierQueueService).removeCourierFromQueue(2L);
        verify(kafkaTemplate).send(eq("courier-events"), anyString());
    }
}

