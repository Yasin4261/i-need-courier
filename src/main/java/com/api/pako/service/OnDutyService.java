package com.api.pako.service;

import com.api.pako.model.OnDutyCourier;
import com.api.pako.repository.OnDutyCourierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class OnDutyService {

    private final OnDutyCourierRepository onDutyCourierRepository;

    public OnDutyService(OnDutyCourierRepository onDutyCourierRepository) {
        this.onDutyCourierRepository = onDutyCourierRepository;
    }

    public List<OnDutyCourier> getAllActiveOrdered() {
        return onDutyCourierRepository.findAllByOrderByOnDutySinceAsc();
    }

    public boolean isCourierOnDuty(Long courierId) {
        return onDutyCourierRepository.findByCourierId(courierId).isPresent();
    }

    @Transactional
    public OnDutyCourier upsertOnDuty(Long courierId, Long shiftId) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        OnDutyCourier entry = onDutyCourierRepository.findByCourierId(courierId)
                .orElseGet(() -> {
                    OnDutyCourier od = new OnDutyCourier();
                    od.setCourierId(courierId);
                    od.setSource("app");
                    od.setCreatedAt(now);
                    return od;
                });

        entry.setShiftId(shiftId);
        entry.setOnDutySince(now);
        entry.setUpdatedAt(now);

        return onDutyCourierRepository.save(entry);
    }

    @Transactional
    public void removeOnDuty(Long courierId) {
        onDutyCourierRepository.deleteByCourierId(courierId);
    }

    /**
     * FIFO kuyruğundan bir sonraki kuryeyi al (order assignment için)
     */
    public OnDutyCourier getNextInQueue() {
        return onDutyCourierRepository.findAllByOrderByOnDutySinceAsc()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Aktif kurye yok"));
    }

    /**
     * On-duty kurye sayısını al
     */
    public long countOnDutyCouriers() {
        return onDutyCourierRepository.count();
    }

    /**
     * Kurye'yi FIFO kuyruğunun sonuna taşı (assignment sonrası)
     */
    @Transactional
    public void moveToEndOfQueue(Long courierId) {
        OnDutyCourier entry = onDutyCourierRepository
                .findByCourierId(courierId)
                .orElseThrow(() -> new RuntimeException("Kurye on-duty değil"));

        entry.setOnDutySince(OffsetDateTime.now(ZoneOffset.UTC));
        entry.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        onDutyCourierRepository.save(entry);
    }
}

