package com.api.pako.dto;

import com.api.pako.model.Business;
import com.api.pako.model.Order;
import com.api.pako.model.enums.OrderPriority;
import com.api.pako.model.enums.OrderStatus;
import com.api.pako.model.enums.PaymentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class CourierOrderResponseTest {

    @Test
    void fromMapsAllCourierVisibleFields() {
        // GIVEN
        var business = new Business();
        business.setId(7L);
        business.setName("Pizza Place");

        var now = LocalDateTime.now();
        var order = new Order();
        order.setId(1L);
        order.setOrderNumber("ORD-123");
        order.setStatus(OrderStatus.ASSIGNED);
        order.setPriority(OrderPriority.NORMAL);
        order.setBusiness(business);
        order.setBusinessPhone("+90 555 111 22 33");
        order.setPickupAddress("Beşiktaş");
        order.setPickupAddressDescription("Köşedeki dükkan");
        order.setPickupContactPerson("Mehmet");
        order.setPickupLatitude(41.0422);
        order.setPickupLongitude(29.0093);
        order.setDeliveryAddress("Kadıköy");
        order.setDeliveryAddressDescription("3. kat");
        order.setDeliveryLatitude(40.9907);
        order.setDeliveryLongitude(29.0245);
        order.setEndCustomerName("Ahmet");
        order.setEndCustomerPhone("+90 555 444 55 66");
        order.setPackageDescription("Pizza");
        order.setPackageWeight(BigDecimal.valueOf(1.5));
        order.setPackageCount(2);
        order.setPaymentType(PaymentType.CASH);
        order.setDeliveryFee(BigDecimal.valueOf(40));
        order.setCollectionAmount(BigDecimal.valueOf(250));
        order.setBusinessNotes("Zili çalma");
        order.setCourierNotes("Teslim edildi");
        order.setScheduledPickupTime(now);
        order.setEstimatedDeliveryTime(now.plusMinutes(30));
        order.setOrderDate(now.minusMinutes(5));
        order.setCreatedAt(now.minusMinutes(10));
        order.setUpdatedAt(now);

        // WHEN
        var underTest = CourierOrderResponse.from(order);

        // THEN
        assertThat(underTest.getOrderNumber()).isEqualTo("ORD-123");
        assertThat(underTest.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
        assertThat(underTest.getPriority()).isEqualTo(OrderPriority.NORMAL);
        assertThat(underTest.getBusinessName()).isEqualTo("Pizza Place");
        assertThat(underTest.getBusinessPhone()).isEqualTo("+90 555 111 22 33");
        assertThat(underTest.getPickupAddress()).isEqualTo("Beşiktaş");
        assertThat(underTest.getPickupAddressDescription()).isEqualTo("Köşedeki dükkan");
        assertThat(underTest.getPickupContactPerson()).isEqualTo("Mehmet");
        assertThat(underTest.getPickupLatitude()).isEqualTo(41.0422);
        assertThat(underTest.getPickupLongitude()).isEqualTo(29.0093);
        assertThat(underTest.getDeliveryAddress()).isEqualTo("Kadıköy");
        assertThat(underTest.getDeliveryLatitude()).isEqualTo(40.9907);
        assertThat(underTest.getDeliveryLongitude()).isEqualTo(29.0245);
        assertThat(underTest.getEndCustomerName()).isEqualTo("Ahmet");
        assertThat(underTest.getEndCustomerPhone()).isEqualTo("+90 555 444 55 66");
        assertThat(underTest.getPackageDescription()).isEqualTo("Pizza");
        assertThat(underTest.getPackageWeight()).isEqualByComparingTo(BigDecimal.valueOf(1.5));
        assertThat(underTest.getPackageCount()).isEqualTo(2);
        assertThat(underTest.getPaymentType()).isEqualTo(PaymentType.CASH);
        assertThat(underTest.getDeliveryFee()).isEqualByComparingTo(BigDecimal.valueOf(40));
        assertThat(underTest.getCollectionAmount()).isEqualByComparingTo(BigDecimal.valueOf(250));
        assertThat(underTest.getBusinessNotes()).isEqualTo("Zili çalma");
        assertThat(underTest.getCourierNotes()).isEqualTo("Teslim edildi");
        assertThat(underTest.getScheduledPickupTime()).isEqualTo(now);
        assertThat(underTest.getEstimatedDeliveryTime()).isEqualTo(now.plusMinutes(30));
        assertThat(underTest.getCreatedAt()).isEqualTo(now.minusMinutes(10));
        assertThat(underTest.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void fromHandlesNullBusinessGracefully() {
        // GIVEN
        var order = new Order();
        order.setOrderNumber("ORD-999");
        order.setBusiness(null);
        order.setBusinessPhone("+90 555 000 00 00");

        // WHEN
        var underTest = CourierOrderResponse.from(order);

        // THEN
        assertThat(underTest.getOrderNumber()).isEqualTo("ORD-999");
        assertThat(underTest.getBusinessName()).isNull();
        assertThat(underTest.getBusinessPhone()).isEqualTo("+90 555 000 00 00");
    }

    @Test
    void doesNotExposeInternalIdentifiers() {
        // Courier view must not leak internal IDs or the business contact person
        // (see courier-dto-fields convention). Enumerated via the Lombok-generated
        // Fields enum rather than reflection.
        var fieldNames = Arrays.stream(CourierOrderResponse.Fields.values())
                .map(Enum::name)
                .collect(Collectors.toSet());

        assertThat(fieldNames)
                .doesNotContain("orderId", "businessId", "businessContactPerson")
                .contains("orderNumber");
    }
}
