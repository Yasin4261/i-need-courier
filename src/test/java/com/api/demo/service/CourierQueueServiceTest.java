package com.api.demo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.ValueOperations;
import java.time.Duration;
import static org.mockito.Mockito.*;

class CourierQueueServiceTest {
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ListOperations<String, Object> listOperations;
    @Mock
    private ValueOperations<String, Object> valueOperations;
    @InjectMocks
    private CourierQueueService courierQueueService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void addCourierToQueue_shouldAddAndSetStatusIfNotInQueue() {
        Long courierId = 1L;
        when(listOperations.range("courier:queue", 0, -1)).thenReturn(java.util.Collections.emptyList());
        when(listOperations.rightPush("courier:queue", courierId)).thenReturn(1L);

        courierQueueService.addCourierToQueue(courierId);

        verify(listOperations).rightPush("courier:queue", courierId);
        verify(valueOperations).set(eq("courier:status:" + courierId), eq("AVAILABLE"), any(Duration.class));
    }

    @Test
    void removeCourierFromQueue_shouldRemoveAndDeleteKeys() {
        Long courierId = 2L;
        courierQueueService.removeCourierFromQueue(courierId);
        verify(listOperations).remove("courier:queue", 0, courierId);
        verify(redisTemplate).delete("courier:status:" + courierId);
        verify(redisTemplate).delete("courier:location:" + courierId);
    }
}

