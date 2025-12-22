package com.api.pako.model.enums;

/**
 * Vardiya durumu enum
 * Kuryelerin vardiya rezervasyon ve check-in/out durumlarını temsil eder
 */
public enum ShiftStatus {
    /**
     * Vardiya rezerve edildi ancak henüz giriş yapılmadı
     */
    RESERVED,

    /**
     * Vardiyaya giriş yapıldı, kurye aktif çalışıyor
     */
    CHECKED_IN,

    /**
     * Vardiyadan çıkış yapıldı, vardiya tamamlandı
     */
    CHECKED_OUT,

    /**
     * Rezervasyon iptal edildi
     */
    CANCELLED,

    /**
     * Kurye rezerve ettiği vardiyaya gelmedi/giriş yapmadı
     */
    NO_SHOW
}

