package com.api.demo.service;

import com.api.demo.dto.CourierDTO;
import com.api.demo.entity.Courier;
import com.api.demo.repository.CourierRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CourierServiceTest {

    @Mock
    private CourierRepository courierRepository;

    @InjectMocks
    private CourierService courierService;

    public CourierServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllCouriers_shouldReturnList() {
        Courier courier1 = new Courier();
        courier1.setId(1L);
        courier1.setName("Ali");
        Courier courier2 = new Courier();
        courier2.setId(2L);
        courier2.setName("Veli");
        when(courierRepository.findAll()).thenReturn(Arrays.asList(courier1, courier2));

        List<CourierDTO> result = courierService.getAllCouriers();
        assertEquals(2, result.size());
        assertEquals("Ali", result.get(0).getName());
        assertEquals("Veli", result.get(1).getName());
    }

    @Test
    void getCourierById_shouldReturnCourier() {
        Courier courier = new Courier();
        courier.setId(1L);
        courier.setName("Ali");
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

        Optional<CourierDTO> result = courierService.getCourierById(1L);
        assertTrue(result.isPresent());
        assertEquals("Ali", result.get().getName());
    }

    @Test
    void getCourierById_shouldReturnEmpty() {
        when(courierRepository.findById(99L)).thenReturn(Optional.empty());
        Optional<CourierDTO> result = courierService.getCourierById(99L);
        assertFalse(result.isPresent());
    }
}

