package com.api.demo.service;

import com.api.demo.entity.Order;
import com.api.demo.entity.Courier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.mockito.Spy;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class OrderAssignmentServiceTest {
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Mock
    private CourierQueueService courierQueueService;
    @Mock
    private CourierService courierService;
    @Spy
    @InjectMocks
    private OrderAssignmentService orderAssignmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void assignOrderToCourier_shouldAssignIfMatchFound() {
        Order order = new Order();
        order.setId(1L);
        OrderAssignmentService.CourierMatch match = Mockito.mock(OrderAssignmentService.CourierMatch.class);
        Mockito.doReturn(match).when(orderAssignmentService).findOptimalCourier(order);
        Mockito.doNothing().when(orderAssignmentService).assignOrderDirectly(anyLong(), anyLong());
        Mockito.doNothing().when(orderAssignmentService).sendOrderAssignmentNotification(any(Order.class), anyLong());
        Mockito.doNothing().when(orderAssignmentService).addOrderToPendingQueue(order);
        Mockito.when(match.getCourierId()).thenReturn(2L);
        OrderAssignmentService.AssignmentResult result = orderAssignmentService.assignOrderToCourier(order);
        Mockito.verify(orderAssignmentService).assignOrderDirectly(order.getId(), 2L);
        Mockito.verify(orderAssignmentService).sendOrderAssignmentNotification(order, 2L);
        assertTrue(result.success());
        assertEquals(2L, result.courierId());
    }

    @Test
    void assignOrderToCourier_shouldNotAssignIfNoMatch() {
        Order order = new Order();
        order.setId(1L);
        Mockito.doReturn(null).when(orderAssignmentService).findOptimalCourier(order);
        Mockito.doNothing().when(orderAssignmentService).addOrderToPendingQueue(order);
        OrderAssignmentService.AssignmentResult result = orderAssignmentService.assignOrderToCourier(order);
        Mockito.verify(orderAssignmentService, Mockito.never()).assignOrderDirectly(anyLong(), anyLong());
        Mockito.verify(orderAssignmentService, Mockito.never()).sendOrderAssignmentNotification(any(Order.class), anyLong());
        assertFalse(result.success());
        assertNull(result.courierId());
    }
}
