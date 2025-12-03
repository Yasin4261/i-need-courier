package com.api.demo.controller;

import com.api.demo.business.dto.OrderCreateRequest;
import com.api.demo.business.dto.OrderResponse;
import com.api.demo.config.container.TestContainersConfiguration;
import com.api.demo.model.enums.OrderPriority;
import com.api.demo.model.enums.OrderStatus;
import com.api.demo.model.enums.PaymentType;
import com.api.demo.repository.BusinessRepository;
import com.api.demo.repository.OrderRepository;
import com.api.demo.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(TestContainersConfiguration.class)
@AutoConfigureMockMvc
public class BusinessOrderControllerIT {

    @Autowired
    private PostgreSQLContainer postgres;

    @Autowired
    BusinessRepository businessRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    JsonMapper objectMapper;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    MockMvcTester mockMvc;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE order_tracking");
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/business/orders creates an order")
    void postToOrdersCreatesAnOrder() throws IOException {
        // GIVEN
        var token = makeToken(42L, "keanu@reeves.com"); // matches with test-migrations directory

        var request = new OrderCreateRequest();
        request.setPickupAddress("Kadıköy Moda Caddesi No:123, Istanbul");
        request.setPickupContactPerson("Ali Veli");
        request.setDeliveryAddress("Beşiktaş Barbaros Bulvarı No:45, Istanbul");
        request.setEndCustomerName("Ahmet Yılmaz");
        request.setEndCustomerPhone("+905551234567");
        request.setPackageDescription("2x Pizza Margherita");
        request.setPackageWeight(BigDecimal.valueOf(1.5));
        request.setPackageCount(2);
        request.setPriority(OrderPriority.NORMAL);
        request.setPaymentType(PaymentType.CASH);
        request.setDeliveryFee(BigDecimal.valueOf(35.50));
        request.setCollectionAmount(BigDecimal.valueOf(0));
        request.setBusinessNotes("Sıcak tutulmalı");

        var expectedResponse = new OrderResponse();
        expectedResponse.setBusinessContactPerson("Keanu Reeves");
        expectedResponse.setBusinessId(42L);
        expectedResponse.setBusinessName("Jack's Burger");
        expectedResponse.setBusinessNotes("Sıcak tutulmalı");
        expectedResponse.setBusinessPhone("05321234567");
        expectedResponse.setCollectionAmount(BigDecimal.ZERO);
        expectedResponse.setDeliveryAddress("Beşiktaş Barbaros Bulvarı No:45, Istanbul");
        expectedResponse.setDeliveryAddressDescription(null);
        expectedResponse.setDeliveryFee(BigDecimal.valueOf(35.5));
        expectedResponse.setEndCustomerName("Ahmet Yılmaz");
        expectedResponse.setEndCustomerPhone("+905551234567");
        expectedResponse.setEstimatedDeliveryTime(null);
        expectedResponse.setOrderId(4L);
        expectedResponse.setOrderNumber("ORD-20251203-001");
        expectedResponse.setPackageCount(2);
        expectedResponse.setPackageDescription("2x Pizza Margherita");
        expectedResponse.setPackageWeight(BigDecimal.valueOf(1.5));
        expectedResponse.setPaymentType(PaymentType.CASH);
        expectedResponse.setPickupAddress("Kadıköy Moda Caddesi No:123, Istanbul");
        expectedResponse.setPickupAddressDescription(null);
        expectedResponse.setPickupContactPerson("Ali Veli");
        expectedResponse.setPriority(OrderPriority.NORMAL);
        expectedResponse.setScheduledPickupTime(null);
        expectedResponse.setStatus(OrderStatus.PENDING);

        // WHEN
        var response = mockMvc
                .post()
                .uri("/api/v1/business/orders")
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .exchange();

        // THEN
        var json = objectMapper.readTree(response.getResponse().getContentAsString());
        var actualDto = objectMapper.treeToValue(json.path("data"), OrderResponse.class);
        assertThat(actualDto).usingRecursiveComparison()
                .ignoringFields("createdAt", "orderDate", "updatedAt")
                .isEqualTo(expectedResponse);
        assertThat(json.path("code").asInt()).isEqualTo(200);
        assertThat(response).hasStatus(HttpStatus.CREATED);
    }

    private String makeToken(Long userId, String email) {
        return jwtTokenProvider.generateToken(userId, email);
    }
}
