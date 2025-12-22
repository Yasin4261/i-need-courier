package com.api.pako.repository;

import com.api.pako.model.Shift;
import com.api.pako.model.enums.ShiftStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Shift entity
 */
@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {

    /**
     * Kuryenin belirli bir tarih aralığındaki vardiyalarını getirir
     */
    List<Shift> findByCourierIdAndStartTimeBetween(Long courierId, LocalDateTime start, LocalDateTime end);

    /**
     * Kuryenin aktif (CHECKED_IN) vardiyasını bulur
     */
    Optional<Shift> findByCourierIdAndStatus(Long courierId, ShiftStatus status);

    /**
     * Kuryenin rezerve olduğu gelecekteki vardiyaları
     */
    @Query("SELECT s FROM Shift s WHERE s.courierId = :courierId AND s.status = 'RESERVED' AND s.startTime > :now ORDER BY s.startTime ASC")
    List<Shift> findUpcomingReservedShifts(@Param("courierId") Long courierId, @Param("now") LocalDateTime now);

    /**
     * Belirli bir tarihteki tüm vardiyalar
     */
    @Query("SELECT s FROM Shift s WHERE DATE(s.startTime) = DATE(:date) ORDER BY s.startTime ASC")
    List<Shift> findShiftsByDate(@Param("date") LocalDateTime date);

    /**
     * Kurye için zaman çakışması kontrolü
     */
    @Query("SELECT COUNT(s) > 0 FROM Shift s WHERE s.courierId = :courierId " +
           "AND s.status NOT IN ('CANCELLED', 'CHECKED_OUT') " +
           "AND ((s.startTime < :endTime AND s.endTime > :startTime))")
    boolean hasOverlappingShift(@Param("courierId") Long courierId,
                                @Param("startTime") LocalDateTime startTime,
                                @Param("endTime") LocalDateTime endTime);

    /**
     * Kuryenin tüm vardiyaları (duruma göre filtreleme)
     */
    List<Shift> findByCourierIdAndStatusOrderByStartTimeDesc(Long courierId, ShiftStatus status);

    /**
     * Belirli bir tarih aralığındaki tüm vardiyalar
     */
    List<Shift> findByStartTimeBetweenOrderByStartTimeAsc(LocalDateTime start, LocalDateTime end);

    /**
     * Check-in yapılması gereken vardiyalar (zamanı gelmiş, henüz check-in yapılmamış)
     */
    @Query("SELECT s FROM Shift s WHERE s.status = 'RESERVED' " +
           "AND s.startTime <= :checkInWindow " +
           "AND s.startTime >= :now " +
           "ORDER BY s.startTime ASC")
    List<Shift> findShiftsReadyForCheckIn(@Param("now") LocalDateTime now,
                                           @Param("checkInWindow") LocalDateTime checkInWindow);
}

