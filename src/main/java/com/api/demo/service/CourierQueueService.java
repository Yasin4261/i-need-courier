package com.api.demo.service;

import com.api.demo.entity.Courier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class CourierQueueService {

    private final RedisTemplate<String, Object> redisTemplate;

    public CourierQueueService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final String COURIER_QUEUE_KEY = "courier:queue";
    private static final String COURIER_STATUS_KEY = "courier:status:";
    private static final String COURIER_LOCATION_KEY = "courier:location:";
    private static final String COURIER_CURRENT_INDEX_KEY = "courier:current:index";

    /**
     * Kurye vardiyaya girdiğinde sıraya ekler
     */
    public void addCourierToQueue(Long courierId) {
        // Kurye zaten sırada mı kontrol et
        if (!isCourierInQueue(courierId)) {
            // Sıranın sonuna ekle
            redisTemplate.opsForList().rightPush(COURIER_QUEUE_KEY, courierId);

            // Kurye durumunu aktif yap
            redisTemplate.opsForValue().set(
                COURIER_STATUS_KEY + courierId,
                "AVAILABLE",
                java.time.Duration.ofHours(12) // 12 saat cache
            );
        }
    }

    /**
     * Kurye vardiyadan çıktığında sıradan çıkarır
     */
    public void removeCourierFromQueue(Long courierId) {
        redisTemplate.opsForList().remove(COURIER_QUEUE_KEY, 0, courierId);
        redisTemplate.delete(COURIER_STATUS_KEY + courierId);
        redisTemplate.delete(COURIER_LOCATION_KEY + courierId);
    }

    /**
     * Sıradaki bir sonraki müsait kuryeyi getirir (Round Robin)
     */
    public Long getNextAvailableCourier() {
        List<Object> courierQueue = redisTemplate.opsForList().range(COURIER_QUEUE_KEY, 0, -1);

        if (courierQueue == null || courierQueue.isEmpty()) {
            return null;
        }

        // Mevcut sıra indeksini al
        Integer currentIndex = (Integer) redisTemplate.opsForValue().get(COURIER_CURRENT_INDEX_KEY);
        if (currentIndex == null) {
            currentIndex = 0;
        }

        // Sırayı dolaşarak müsait kurye bul
        int startIndex = currentIndex;
        int queueSize = courierQueue.size();

        for (int i = 0; i < queueSize; i++) {
            int checkIndex = (startIndex + i) % queueSize;
            Long courierId = Long.valueOf(courierQueue.get(checkIndex).toString());

            if (isCourierAvailable(courierId)) {
                // Sıra indeksini güncelle (bir sonraki kuryeyi işaret etsin)
                int nextIndex = (checkIndex + 1) % queueSize;
                redisTemplate.opsForValue().set(COURIER_CURRENT_INDEX_KEY, nextIndex);

                return courierId;
            }
        }

        return null; // Hiç müsait kurye yok
    }

    /**
     * Kurye müsait mi kontrol eder
     */
    public boolean isCourierAvailable(Long courierId) {
        String status = (String) redisTemplate.opsForValue().get(COURIER_STATUS_KEY + courierId);
        return "AVAILABLE".equals(status);
    }

    /**
     * Kurye durumunu günceller
     */
    public void updateCourierStatus(Long courierId, String status) {
        redisTemplate.opsForValue().set(
            COURIER_STATUS_KEY + courierId,
            status,
            java.time.Duration.ofHours(12)
        );
    }

    /**
     * Kurye lokasyonunu günceller
     */
    public void updateCourierLocation(Long courierId, double latitude, double longitude) {
        String locationData = latitude + "," + longitude + "," + LocalDateTime.now();
        redisTemplate.opsForValue().set(
            COURIER_LOCATION_KEY + courierId,
            locationData,
            java.time.Duration.ofHours(2)
        );
    }

    /**
     * Kurye sırada mı kontrol eder
     */
    public boolean isCourierInQueue(Long courierId) {
        List<Object> courierQueue = redisTemplate.opsForList().range(COURIER_QUEUE_KEY, 0, -1);
        return courierQueue != null && courierQueue.contains(courierId);
    }

    /**
     * Aktif kurye sırasını getirir
     */
    public List<Object> getCourierQueue() {
        return redisTemplate.opsForList().range(COURIER_QUEUE_KEY, 0, -1);
    }

    /**
     * Sırayı temizler (debug/admin için)
     */
    public void clearQueue() {
        redisTemplate.delete(COURIER_QUEUE_KEY);
        redisTemplate.delete(COURIER_CURRENT_INDEX_KEY);
    }

    /**
     * Sıra istatistiklerini getirir
     */
    public QueueStatistics getQueueStatistics() {
        List<Object> queue = getCourierQueue();
        int totalCouriers = queue != null ? queue.size() : 0;

        long availableCouriers = 0;
        if (queue != null) {
            availableCouriers = queue.stream()
                .mapToLong(id -> Long.valueOf(id.toString()))
                .mapToInt(id -> isCourierAvailable(id) ? 1 : 0)
                .sum();
        }

        Integer currentIndex = (Integer) redisTemplate.opsForValue().get(COURIER_CURRENT_INDEX_KEY);

        return new QueueStatistics(totalCouriers, (int) availableCouriers, currentIndex != null ? currentIndex : 0);
    }

    // İstatistik sınıfı
    public static class QueueStatistics {
        private int totalCouriers;
        private int availableCouriers;
        private int currentIndex;

        public QueueStatistics(int totalCouriers, int availableCouriers, int currentIndex) {
            this.totalCouriers = totalCouriers;
            this.availableCouriers = availableCouriers;
            this.currentIndex = currentIndex;
        }

        // Getters
        public int getTotalCouriers() { return totalCouriers; }
        public int getAvailableCouriers() { return availableCouriers; }
        public int getCurrentIndex() { return currentIndex; }
    }
}
