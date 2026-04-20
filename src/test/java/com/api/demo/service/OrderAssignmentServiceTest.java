package com.api.demo.service;

import com.api.demo.exception.AssignmentExpiredException;
import com.api.demo.exception.AssignmentNotFoundException;
import com.api.demo.exception.AssignmentNotOwnedException;
import com.api.demo.exception.BusinessException;
import com.api.demo.exception.InvalidAssignmentStatusException;
import com.api.demo.exception.NoCourierAvailableException;
import com.api.demo.model.Courier;
import com.api.demo.model.OnDutyCourier;
import com.api.demo.model.Order;
import com.api.demo.model.OrderAssignment;
import com.api.demo.model.enums.AssignmentStatus;
import com.api.demo.model.enums.AssignmentType;
import com.api.demo.model.enums.OrderStatus;
import com.api.demo.repository.CourierRepository;
import com.api.demo.repository.OrderAssignmentRepository;
import com.api.demo.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderAssignmentServiceTest {

    @Mock
    private OnDutyService onDutyService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderAssignmentRepository orderAssignmentRepository;

    @Mock
    private WebSocketNotificationService notificationService;

    @Mock
    private CourierRepository courierRepository;

    @InjectMocks
    private OrderAssignmentService underTest;

    private Order testOrder;
    private Courier testCourier;
    private OnDutyCourier testOnDutyCourier;
    private OrderAssignment testAssignment;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(underTest, "assignmentTimeoutMinutes", 2);

        testCourier = new Courier();
        testCourier.setId(10L);
        testCourier.setName("Test Kurye");

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setPickupAddress("Beşiktaş");
        testOrder.setDeliveryAddress("Kadıköy");
        testOrder.setPackageDescription("Pizza");
        testOrder.setEndCustomerName("Ahmet");

        testOnDutyCourier = new OnDutyCourier();
        testOnDutyCourier.setCourierId(10L);
        testOnDutyCourier.setOnDutySince(OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(30));

        testAssignment = new OrderAssignment();
        testAssignment.setId(100L);
        testAssignment.setOrderId(1L);
        testAssignment.setCourierId(10L);
        testAssignment.setStatus(AssignmentStatus.PENDING);
        testAssignment.setAssignmentType(AssignmentType.AUTO);
        testAssignment.setAssignedAt(OffsetDateTime.now(ZoneOffset.UTC));
        testAssignment.setTimeoutAt(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(2));
    }

    // =========================================================================
    // assignToNextAvailableCourier
    // =========================================================================
    @Nested
    @DisplayName("assignToNextAvailableCourier - Sipariş Atama")
    class AssignToNextAvailableCourierTests {

        @Test
        @DisplayName("Başarılı atama: Sipariş oluşturulunca FIFO sırasına göre kuryeye atanır")
        void shouldAssignOrderToNextCourierInFIFOQueue() {
            // GIVEN
            when(orderAssignmentRepository.existsByOrderIdAndStatus(1L, AssignmentStatus.PENDING))
                    .thenReturn(false);
            when(onDutyService.getNextInQueue()).thenReturn(testOnDutyCourier);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(courierRepository.findById(10L)).thenReturn(Optional.of(testCourier));
            when(orderAssignmentRepository.save(any(OrderAssignment.class))).thenAnswer(inv -> {
                OrderAssignment a = inv.getArgument(0);
                a.setId(100L);
                return a;
            });

            // WHEN
            OrderAssignment result = underTest.assignToNextAvailableCourier(1L);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getOrderId()).isEqualTo(1L);
            assertThat(result.getCourierId()).isEqualTo(10L);
            assertThat(result.getStatus()).isEqualTo(AssignmentStatus.PENDING);
            assertThat(result.getAssignmentType()).isEqualTo(AssignmentType.AUTO);
            assertThat(result.getTimeoutAt()).isAfter(result.getAssignedAt());

            // Order güncellendi mi?
            verify(orderRepository).save(testOrder);
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(testOrder.getCourier()).isEqualTo(testCourier);
        }

        @Test
        @DisplayName("Aktif kurye yoksa NoCourierAvailableException fırlatılır")
        void shouldThrowWhenNoCourierAvailable() {
            // GIVEN
            when(orderAssignmentRepository.existsByOrderIdAndStatus(1L, AssignmentStatus.PENDING))
                    .thenReturn(false);
            when(onDutyService.getNextInQueue()).thenThrow(new RuntimeException("Aktif kurye yok"));

            // WHEN & THEN
            assertThatThrownBy(() -> underTest.assignToNextAvailableCourier(1L))
                    .isInstanceOf(NoCourierAvailableException.class)
                    .hasMessageContaining("aktif kurye yok");
        }

        @Test
        @DisplayName("Siparişin zaten PENDING assignment'ı varsa duplicate oluşturulmaz")
        void shouldReturnExistingAssignmentWhenDuplicatePending() {
            // GIVEN
            when(orderAssignmentRepository.existsByOrderIdAndStatus(1L, AssignmentStatus.PENDING))
                    .thenReturn(true);
            when(orderAssignmentRepository.findByOrderIdAndStatus(1L, AssignmentStatus.PENDING))
                    .thenReturn(Optional.of(testAssignment));

            // WHEN
            OrderAssignment result = underTest.assignToNextAvailableCourier(1L);

            // THEN
            assertThat(result).isSameAs(testAssignment);
            verify(onDutyService, never()).getNextInQueue();
            verify(orderAssignmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Sipariş bulunamazsa BusinessException fırlatılır")
        void shouldThrowWhenOrderNotFound() {
            // GIVEN
            when(orderAssignmentRepository.existsByOrderIdAndStatus(1L, AssignmentStatus.PENDING))
                    .thenReturn(false);
            when(onDutyService.getNextInQueue()).thenReturn(testOnDutyCourier);
            when(orderRepository.findById(1L)).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> underTest.assignToNextAvailableCourier(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Sipariş bulunamadı");
        }

        @Test
        @DisplayName("Kurye bulunamazsa BusinessException fırlatılır")
        void shouldThrowWhenCourierNotFound() {
            // GIVEN
            when(orderAssignmentRepository.existsByOrderIdAndStatus(1L, AssignmentStatus.PENDING))
                    .thenReturn(false);
            when(onDutyService.getNextInQueue()).thenReturn(testOnDutyCourier);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(courierRepository.findById(10L)).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> underTest.assignToNextAvailableCourier(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Kurye bulunamadı");
        }

        @Test
        @DisplayName("WebSocket bildirimi başarısız olsa bile assignment oluşturulur")
        void shouldCreateAssignmentEvenIfWebSocketFails() {
            // GIVEN
            when(orderAssignmentRepository.existsByOrderIdAndStatus(1L, AssignmentStatus.PENDING))
                    .thenReturn(false);
            when(onDutyService.getNextInQueue()).thenReturn(testOnDutyCourier);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(courierRepository.findById(10L)).thenReturn(Optional.of(testCourier));
            when(orderAssignmentRepository.save(any(OrderAssignment.class))).thenAnswer(inv -> {
                OrderAssignment a = inv.getArgument(0);
                a.setId(100L);
                return a;
            });
            doThrow(new RuntimeException("WebSocket error"))
                    .when(notificationService).notifyNewAssignment(any(), anyMap());

            // WHEN
            OrderAssignment result = underTest.assignToNextAvailableCourier(1L);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getCourierId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("REASSIGNMENT: Tek kurye varsa ve timeout olduysa hata fırlatılır")
        void shouldThrowWhenReassignmentWithOnlyOneCourier() {
            // GIVEN
            when(orderAssignmentRepository.existsByOrderIdAndStatus(1L, AssignmentStatus.PENDING))
                    .thenReturn(false);

            OrderAssignment timedOut = new OrderAssignment();
            timedOut.setCourierId(10L);
            timedOut.setStatus(AssignmentStatus.TIMEOUT);

            when(orderAssignmentRepository.findByOrderIdAndStatusOrderByAssignedAtDesc(1L, AssignmentStatus.TIMEOUT))
                    .thenReturn(List.of(timedOut));
            when(onDutyService.countOnDutyCouriers()).thenReturn(1L);

            // WHEN & THEN
            assertThatThrownBy(() ->
                    underTest.assignToNextAvailableCourier(1L, AssignmentType.REASSIGNMENT))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("yeterli kurye yok");
        }

        @Test
        @DisplayName("REASSIGNMENT: Birden fazla kurye varsa başarıyla atanır")
        void shouldReassignWhenMultipleCouriersAvailable() {
            // GIVEN
            when(orderAssignmentRepository.existsByOrderIdAndStatus(1L, AssignmentStatus.PENDING))
                    .thenReturn(false);

            OrderAssignment timedOut = new OrderAssignment();
            timedOut.setCourierId(99L); // farklı kurye timeout oldu

            when(orderAssignmentRepository.findByOrderIdAndStatusOrderByAssignedAtDesc(1L, AssignmentStatus.TIMEOUT))
                    .thenReturn(List.of(timedOut));
            when(onDutyService.countOnDutyCouriers()).thenReturn(3L);
            when(onDutyService.getNextInQueue()).thenReturn(testOnDutyCourier);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(courierRepository.findById(10L)).thenReturn(Optional.of(testCourier));
            when(orderAssignmentRepository.save(any(OrderAssignment.class))).thenAnswer(inv -> {
                OrderAssignment a = inv.getArgument(0);
                a.setId(101L);
                return a;
            });

            // WHEN
            OrderAssignment result = underTest.assignToNextAvailableCourier(1L, AssignmentType.REASSIGNMENT);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getAssignmentType()).isEqualTo(AssignmentType.REASSIGNMENT);
        }
    }

    // =========================================================================
    // acceptAssignment
    // =========================================================================
    @Nested
    @DisplayName("acceptAssignment - Kurye Onayı")
    class AcceptAssignmentTests {

        @Test
        @DisplayName("Kurye atamayı başarıyla kabul eder")
        void shouldAcceptAssignmentSuccessfully() {
            // GIVEN
            when(orderAssignmentRepository.findById(100L)).thenReturn(Optional.of(testAssignment));
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(courierRepository.findById(10L)).thenReturn(Optional.of(testCourier));
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

            // WHEN
            underTest.acceptAssignment(100L, 10L);

            // THEN
            ArgumentCaptor<OrderAssignment> captor = ArgumentCaptor.forClass(OrderAssignment.class);
            verify(orderAssignmentRepository).save(captor.capture());

            OrderAssignment saved = captor.getValue();
            assertThat(saved.getStatus()).isEqualTo(AssignmentStatus.ACCEPTED);
            assertThat(saved.getResponseAt()).isNotNull();

            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
            assertThat(testOrder.getCourier()).isEqualTo(testCourier);

            verify(onDutyService).moveToEndOfQueue(10L);
        }

        @Test
        @DisplayName("Atama bulunamazsa AssignmentNotFoundException fırlatılır")
        void shouldThrowWhenAssignmentNotFound() {
            // GIVEN
            when(orderAssignmentRepository.findById(999L)).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> underTest.acceptAssignment(999L, 10L))
                    .isInstanceOf(AssignmentNotFoundException.class);
        }

        @Test
        @DisplayName("Başka kuryenin atamasını kabul edemez")
        void shouldThrowWhenCourierDoesNotOwnAssignment() {
            // GIVEN
            when(orderAssignmentRepository.findById(100L)).thenReturn(Optional.of(testAssignment));

            // WHEN & THEN
            assertThatThrownBy(() -> underTest.acceptAssignment(100L, 999L))
                    .isInstanceOf(AssignmentNotOwnedException.class);
        }

        @Test
        @DisplayName("PENDING olmayan atama kabul edilemez")
        void shouldThrowWhenAssignmentNotPending() {
            // GIVEN
            testAssignment.setStatus(AssignmentStatus.ACCEPTED);
            when(orderAssignmentRepository.findById(100L)).thenReturn(Optional.of(testAssignment));

            // WHEN & THEN
            assertThatThrownBy(() -> underTest.acceptAssignment(100L, 10L))
                    .isInstanceOf(InvalidAssignmentStatusException.class);
        }

        @Test
        @DisplayName("Süresi dolmuş atama kabul edilemez")
        void shouldThrowWhenAssignmentExpired() {
            // GIVEN
            testAssignment.setTimeoutAt(OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(5));
            when(orderAssignmentRepository.findById(100L)).thenReturn(Optional.of(testAssignment));

            // WHEN & THEN
            assertThatThrownBy(() -> underTest.acceptAssignment(100L, 10L))
                    .isInstanceOf(AssignmentExpiredException.class);
        }

        @Test
        @DisplayName("moveToEndOfQueue başarısız olsa bile accept tamamlanır")
        void shouldCompleteAcceptEvenIfMoveToEndFails() {
            // GIVEN
            when(orderAssignmentRepository.findById(100L)).thenReturn(Optional.of(testAssignment));
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(courierRepository.findById(10L)).thenReturn(Optional.of(testCourier));
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
            doThrow(new RuntimeException("Queue error")).when(onDutyService).moveToEndOfQueue(10L);

            // WHEN - hata fırlatmamalı
            underTest.acceptAssignment(100L, 10L);

            // THEN
            assertThat(testAssignment.getStatus()).isEqualTo(AssignmentStatus.ACCEPTED);
        }
    }

    // =========================================================================
    // rejectAssignment
    // =========================================================================
    @Nested
    @DisplayName("rejectAssignment - Kurye Reddi")
    class RejectAssignmentTests {

        @Test
        @DisplayName("Kurye atamayı reddeder ve sipariş sonraki kuryeye atanır")
        void shouldRejectAndReassign() {
            // GIVEN
            when(orderAssignmentRepository.findById(100L)).thenReturn(Optional.of(testAssignment));

            // Reassignment için gerekli mock'lar
            when(orderAssignmentRepository.existsByOrderIdAndStatus(1L, AssignmentStatus.PENDING))
                    .thenReturn(false);

            OnDutyCourier nextCourier = new OnDutyCourier();
            nextCourier.setCourierId(20L);
            nextCourier.setOnDutySince(OffsetDateTime.now(ZoneOffset.UTC));

            Courier courier2 = new Courier();
            courier2.setId(20L);

            when(orderAssignmentRepository.findByOrderIdAndStatusOrderByAssignedAtDesc(1L, AssignmentStatus.TIMEOUT))
                    .thenReturn(List.of());
            when(onDutyService.getNextInQueue()).thenReturn(nextCourier);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(courierRepository.findById(20L)).thenReturn(Optional.of(courier2));
            when(orderAssignmentRepository.save(any())).thenAnswer(inv -> {
                OrderAssignment a = inv.getArgument(0);
                if (a.getId() == null) a.setId(101L);
                return a;
            });

            // WHEN
            underTest.rejectAssignment(100L, 10L, "Meşgulüm");

            // THEN
            assertThat(testAssignment.getStatus()).isEqualTo(AssignmentStatus.REJECTED);
            assertThat(testAssignment.getRejectionReason()).isEqualTo("Meşgulüm");
            assertThat(testAssignment.getResponseAt()).isNotNull();

            // Yeni atama yapıldı mı? (save 2 kez çağrılmalı: reject + reassign)
            verify(orderAssignmentRepository, atLeast(2)).save(any(OrderAssignment.class));
        }

        @Test
        @DisplayName("Başka kuryenin atamasını reddedemez")
        void shouldThrowWhenRejectingOtherCouriersAssignment() {
            // GIVEN
            when(orderAssignmentRepository.findById(100L)).thenReturn(Optional.of(testAssignment));

            // WHEN & THEN
            assertThatThrownBy(() -> underTest.rejectAssignment(100L, 999L, "neden"))
                    .isInstanceOf(AssignmentNotOwnedException.class);
        }

        @Test
        @DisplayName("PENDING olmayan atama reddedilemez")
        void shouldThrowWhenRejectingNonPendingAssignment() {
            // GIVEN
            testAssignment.setStatus(AssignmentStatus.ACCEPTED);
            when(orderAssignmentRepository.findById(100L)).thenReturn(Optional.of(testAssignment));

            // WHEN & THEN
            assertThatThrownBy(() -> underTest.rejectAssignment(100L, 10L, "neden"))
                    .isInstanceOf(InvalidAssignmentStatusException.class);
        }

        @Test
        @DisplayName("Reddedilip yeniden atama yapılamazsa BusinessException fırlatılır")
        void shouldThrowWhenReassignmentFailsAfterRejection() {
            // GIVEN
            when(orderAssignmentRepository.findById(100L)).thenReturn(Optional.of(testAssignment));
            when(orderAssignmentRepository.save(any())).thenReturn(testAssignment);
            when(orderAssignmentRepository.existsByOrderIdAndStatus(1L, AssignmentStatus.PENDING))
                    .thenReturn(false);
            when(onDutyService.getNextInQueue()).thenThrow(new RuntimeException("Aktif kurye yok"));

            // WHEN & THEN
            // NoCourierAvailableException fırlatılır, reject içinde BusinessException'a sarılır
            assertThatThrownBy(() -> underTest.rejectAssignment(100L, 10L, "Meşgulüm"))
                    .isInstanceOf(RuntimeException.class);

            // Red yine de kaydedilir
            assertThat(testAssignment.getStatus()).isEqualTo(AssignmentStatus.REJECTED);
        }
    }

    // =========================================================================
    // getPendingAssignments
    // =========================================================================
    @Nested
    @DisplayName("getPendingAssignments - Bekleyen Atamalar")
    class GetPendingAssignmentsTests {

        @Test
        @DisplayName("Kuryenin bekleyen atamalarını döner")
        void shouldReturnPendingAssignments() {
            // GIVEN
            when(orderAssignmentRepository.findValidPendingAssignmentsByCourierId(10L))
                    .thenReturn(List.of(testAssignment));

            // WHEN
            List<OrderAssignment> result = underTest.getPendingAssignments(10L);

            // THEN
            assertThat(result)
                    .hasSize(1)
                    .first()
                    .satisfies(a -> {
                        assertThat(a.getCourierId()).isEqualTo(10L);
                        assertThat(a.getStatus()).isEqualTo(AssignmentStatus.PENDING);
                    });
        }

        @Test
        @DisplayName("Bekleyen atama yoksa boş liste döner")
        void shouldReturnEmptyWhenNoPendingAssignments() {
            // GIVEN
            when(orderAssignmentRepository.findValidPendingAssignmentsByCourierId(10L))
                    .thenReturn(List.of());

            // WHEN
            List<OrderAssignment> result = underTest.getPendingAssignments(10L);

            // THEN
            assertThat(result).isEmpty();
        }
    }

    // =========================================================================
    // checkTimeouts
    // =========================================================================
    @Nested
    @DisplayName("checkTimeouts - Timeout Kontrolü")
    class CheckTimeoutsTests {

        @Test
        @DisplayName("Süresi dolmuş atamalar TIMEOUT yapılır ve reassign edilir")
        void shouldTimeoutExpiredAssignmentsAndReassign() {
            // GIVEN
            OrderAssignment expired = new OrderAssignment();
            expired.setId(200L);
            expired.setOrderId(5L);
            expired.setCourierId(10L);
            expired.setStatus(AssignmentStatus.PENDING);
            expired.setTimeoutAt(OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(1));

            when(orderAssignmentRepository.findByStatusAndTimeoutAtBefore(eq(AssignmentStatus.PENDING), any()))
                    .thenReturn(List.of(expired));
            when(orderAssignmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // Reassignment mock
            when(orderAssignmentRepository.existsByOrderIdAndStatus(5L, AssignmentStatus.PENDING))
                    .thenReturn(false);
            when(orderAssignmentRepository.findByOrderIdAndStatusOrderByAssignedAtDesc(5L, AssignmentStatus.TIMEOUT))
                    .thenReturn(List.of(expired));
            when(onDutyService.countOnDutyCouriers()).thenReturn(3L);
            when(onDutyService.getNextInQueue()).thenReturn(testOnDutyCourier);
            when(orderRepository.findById(5L)).thenReturn(Optional.of(testOrder));
            when(courierRepository.findById(10L)).thenReturn(Optional.of(testCourier));

            // WHEN
            underTest.checkTimeouts();

            // THEN
            assertThat(expired.getStatus()).isEqualTo(AssignmentStatus.TIMEOUT);
            assertThat(expired.getResponseAt()).isNotNull();
            assertThat(expired.getRejectionReason()).contains("süre");

            verify(notificationService).notifyAssignmentTimeout(10L, 200L);
        }

        @Test
        @DisplayName("Timeout olmuş atama yoksa hiçbir işlem yapılmaz")
        void shouldDoNothingWhenNoTimeouts() {
            // GIVEN
            when(orderAssignmentRepository.findByStatusAndTimeoutAtBefore(eq(AssignmentStatus.PENDING), any()))
                    .thenReturn(List.of());

            // WHEN
            underTest.checkTimeouts();

            // THEN
            verify(orderAssignmentRepository, never()).save(any());
            verify(notificationService, never()).notifyAssignmentTimeout(anyLong(), anyLong());
        }
    }
}

