package com.api.pako.controller;

import com.api.pako.exception.InvalidOrderOperationException;
import com.api.pako.exception.UnauthorizedAccessException;
import com.api.pako.model.Courier;
import com.api.pako.model.Order;
import com.api.pako.model.enums.OrderStatus;
import com.api.pako.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourierOrderControllerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CourierOrderController underTest;

    @Test
    void getMyOrdersReturnsDtosSortedNewestFirst() {
        // GIVEN
        when(authentication.getPrincipal()).thenReturn(10L);

        var older = order(1L, "ORD-OLD", OrderStatus.DELIVERED);
        older.setUpdatedAt(LocalDateTime.now().minusHours(2));
        var newer = order(2L, "ORD-NEW", OrderStatus.IN_TRANSIT);
        newer.setUpdatedAt(LocalDateTime.now());

        when(orderRepository.findByCourierId(10L)).thenReturn(List.of(older, newer));

        // WHEN
        var result = underTest.getMyOrders(authentication, null);

        // THEN
        assertThat(result.getData())
                .extracting("orderNumber")
                .containsExactly("ORD-NEW", "ORD-OLD");
    }

    @Test
    void getMyOrdersWithStatusFiltersByStatus() {
        // GIVEN
        when(authentication.getPrincipal()).thenReturn(10L);
        var delivered = order(1L, "ORD-1", OrderStatus.DELIVERED);
        when(orderRepository.findByCourierIdAndStatus(10L, OrderStatus.DELIVERED))
                .thenReturn(List.of(delivered));

        // WHEN
        var result = underTest.getMyOrders(authentication, OrderStatus.DELIVERED);

        // THEN
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getOrderNumber()).isEqualTo("ORD-1");
        assertThat(result.getData().get(0).getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    void getOrderReturnsDtoWhenCourierOwnsIt() {
        // GIVEN
        when(authentication.getPrincipal()).thenReturn(10L);
        var order = order(1L, "ORD-1", OrderStatus.ASSIGNED);
        order.setCourier(courier(10L));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // WHEN
        var result = underTest.getOrder(authentication, 1L);

        // THEN
        assertThat(result.getData().getOrderNumber()).isEqualTo("ORD-1");
    }

    @Test
    void getOrderThrowsWhenCourierDoesNotOwnIt() {
        // GIVEN
        when(authentication.getPrincipal()).thenReturn(10L);
        var order = order(1L, "ORD-1", OrderStatus.ASSIGNED);
        order.setCourier(courier(99L));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // WHEN & THEN
        assertThatThrownBy(() -> underTest.getOrder(authentication, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("size atanmamış");
    }

    @Test
    void pickupOrderTransitionsAssignedToPickedUpAndReturnsDto() {
        // GIVEN
        when(authentication.getPrincipal()).thenReturn(10L);
        var order = order(1L, "ORD-1", OrderStatus.ASSIGNED);
        order.setCourier(courier(10L));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // WHEN
        var result = underTest.pickupOrder(authentication, 1L, "kapıda");

        // THEN
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PICKED_UP);
        assertThat(order.getCourierNotes()).isEqualTo("kapıda");
        assertThat(result.getData().getStatus()).isEqualTo(OrderStatus.PICKED_UP);
        verify(orderRepository).save(order);
    }

    @Test
    void pickupOrderThrowsWhenStatusNotAssigned() {
        // GIVEN
        when(authentication.getPrincipal()).thenReturn(10L);
        var order = order(1L, "ORD-1", OrderStatus.PICKED_UP);
        order.setCourier(courier(10L));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // WHEN & THEN
        assertThatThrownBy(() -> underTest.pickupOrder(authentication, 1L, null))
                .isInstanceOf(InvalidOrderOperationException.class);
    }

    @Test
    void pickupOrderThrowsWhenCourierNotAssigned() {
        // GIVEN
        when(authentication.getPrincipal()).thenReturn(10L);
        var order = order(1L, "ORD-1", OrderStatus.ASSIGNED);
        order.setCourier(null);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // WHEN & THEN
        assertThatThrownBy(() -> underTest.pickupOrder(authentication, 1L, null))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    private static Order order(Long id, String number, OrderStatus status) {
        var order = new Order();
        order.setId(id);
        order.setOrderNumber(number);
        order.setStatus(status);
        return order;
    }

    private static Courier courier(Long id) {
        var courier = new Courier();
        courier.setId(id);
        return courier;
    }
}
