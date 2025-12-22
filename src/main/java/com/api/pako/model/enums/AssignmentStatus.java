package com.api.pako.model.enums;

public enum AssignmentStatus {
    PENDING,      // Kurye cevabı bekleniyor
    ACCEPTED,     // Kurye kabul etti
    REJECTED,     // Kurye reddetti
    TIMEOUT       // Zaman aşımı (otomatik red)
}

