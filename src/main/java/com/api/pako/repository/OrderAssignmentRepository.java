package com.api.pako.repository;

import com.api.pako.model.OrderAssignment;
import com.api.pako.model.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderAssignmentRepository extends JpaRepository<OrderAssignment, Long> {

    List<OrderAssignment> findByOrderIdOrderByAssignedAtDesc(Long orderId);

    List<OrderAssignment> findByCourierIdAndStatusOrderByAssignedAtDesc(Long courierId, AssignmentStatus status);

    List<OrderAssignment> findByOrderIdAndStatusOrderByAssignedAtDesc(Long orderId, AssignmentStatus status);

    Optional<OrderAssignment> findFirstByOrderIdAndStatusOrderByAssignedAtDesc(Long orderId, AssignmentStatus status);

    @Query("SELECT a FROM OrderAssignment a WHERE a.status = 'PENDING' AND a.timeoutAt <= :now")
    List<OrderAssignment> findTimedOutAssignments(OffsetDateTime now);

    List<OrderAssignment> findByStatusAndTimeoutAtBefore(AssignmentStatus status, OffsetDateTime now);

    // Check if order already has a pending assignment (to prevent duplicates)
    boolean existsByOrderIdAndStatus(Long orderId, AssignmentStatus status);

    // Find active (pending) assignment for an order
    Optional<OrderAssignment> findByOrderIdAndStatus(Long orderId, AssignmentStatus status);

    // Find valid (non-expired) pending assignments for a courier
    @Query("SELECT a FROM OrderAssignment a WHERE a.courierId = :courierId " +
           "AND a.status = 'PENDING' " +
           "AND (a.timeoutAt IS NULL OR a.timeoutAt > CURRENT_TIMESTAMP) " +
           "ORDER BY a.assignedAt DESC")
    List<OrderAssignment> findValidPendingAssignmentsByCourierId(@Param("courierId") Long courierId);
}

