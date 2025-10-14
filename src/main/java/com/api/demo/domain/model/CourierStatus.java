package com.api.demo.domain.model;

public enum CourierStatus {
    INACTIVE,    // Kayıt olmuş ama henüz aktif değil
    ONLINE,      // Aktif ve sipariş alabilir
    OFFLINE,     // Çevrimdışı
    BUSY,        // Sipariş taşıyor
    SUSPENDED    // Askıya alınmış
}
