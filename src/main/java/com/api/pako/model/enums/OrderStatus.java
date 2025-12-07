package com.api.pako.model.enums;

/**
 * Order status enum matching database order_status type
 */
public enum OrderStatus {
    PENDING,      // Order created, waiting for courier assignment
    ASSIGNED,     // Courier assigned to order
    PICKED_UP,    // Courier picked up the package
    IN_TRANSIT,   // Package is on the way
    DELIVERED,    // Package delivered successfully
    CANCELLED,    // Order cancelled
    RETURNED      // Package returned to sender
}

