package com.api.demo.controller;

import com.api.demo.dto.CourierDTO;
import com.api.demo.service.CourierService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import java.util.Optional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

@WebMvcTest(CourierController.class)
public class CourierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourierService courierService;

    @Test
    void getAllCouriers_shouldReturnList() throws Exception {
        CourierDTO courier1 = new CourierDTO();
        courier1.setId(1L);
        courier1.setName("Ali");
        CourierDTO courier2 = new CourierDTO();
        courier2.setId(2L);
        courier2.setName("Veli");
        when(courierService.getAllCouriers()).thenReturn(Arrays.asList(courier1, courier2));

        mockMvc.perform(get("/api/couriers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Ali"))
                .andExpect(jsonPath("$[1].name").value("Veli"));
    }

    @Test
    void getCourierById_shouldReturnCourier() throws Exception {
        CourierDTO courier = new CourierDTO();
        courier.setId(1L);
        courier.setName("Ali");
        when(courierService.getCourierById(1L)).thenReturn(Optional.of(courier));

        mockMvc.perform(get("/api/couriers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ali"));
    }

    @Test
    void getCourierById_shouldReturnNotFound() throws Exception {
        when(courierService.getCourierById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/couriers/99"))
                .andExpect(status().isNotFound());
    }
}

