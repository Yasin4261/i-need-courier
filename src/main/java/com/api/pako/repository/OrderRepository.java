package com.api.pako.repository;

import com.api.pako.model.Order;
import com.api.pako.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Order entity
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find order by order number
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Find all orders for a specific business
     */
    List<Order> findByBusinessId(Long businessId);

    /**
     * Find orders by business and status
     */
    List<Order> findByBusinessIdAndStatus(Long businessId, OrderStatus status);

    /**
     * Find orders by business, ordered by creation date descending
     */
    List<Order> findByBusinessIdOrderByCreatedAtDesc(Long businessId);

    /**
     * Find orders by business and status, ordered by priority and creation date
     */
    @Query("SELECT o FROM Order o WHERE o.business.id = :businessId AND o.status = :status " +
           "ORDER BY o.priority DESC, o.createdAt ASC")
    List<Order> findByBusinessIdAndStatusOrderByPriorityAndCreatedAt(
            @Param("businessId") Long businessId,
            @Param("status") OrderStatus status
    );

    /**
     * Find pending orders for a business
     */
    @Query("SELECT o FROM Order o WHERE o.business.id = :businessId AND o.status = 'PENDING' " +
           "ORDER BY o.priority DESC, o.createdAt ASC")
    List<Order> findPendingOrdersByBusiness(@Param("businessId") Long businessId);

    /**
     * Count orders by business and status
     */
    long countByBusinessIdAndStatus(Long businessId, OrderStatus status);

    /**
     * Count total orders by business
     */
    long countByBusinessId(Long businessId);

    /**
     * Check if order exists by order number
     */
    boolean existsByOrderNumber(String orderNumber);

    /**
     * Find orders by courier
     */
    List<Order> findByCourierId(Long courierId);

    /**
     * Find orders by courier and status
     */
    List<Order> findByCourierIdAndStatus(Long courierId, OrderStatus status);
}

