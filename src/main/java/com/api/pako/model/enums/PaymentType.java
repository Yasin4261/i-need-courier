package com.api.pako.model.enums;

/**
 * Payment type enum matching database payment_type type
 */
public enum PaymentType {
    CASH,                // Cash payment
    CREDIT_CARD,        // Credit card payment
    BUSINESS_ACCOUNT,   // Business account payment
    CASH_ON_DELIVERY,   // Cash on delivery (COD)
    ONLINE              // Online payment
}

