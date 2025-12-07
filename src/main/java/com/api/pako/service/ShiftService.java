package com.api.pako.service;

import com.api.pako.dto.*;
import com.api.pako.exception.BusinessException;
import com.api.pako.model.Courier;
import com.api.pako.model.Shift;
import com.api.pako.model.ShiftTemplate;
import com.api.pako.model.enums.ShiftStatus;
import com.api.pako.repository.CourierRepository;
import com.api.pako.repository.ShiftRepository;
import com.api.pako.repository.ShiftTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Vardiya yönetim servisi
 * Kuryelerin vardiya rezervasyonu, check-in/out işlemlerini yönetir
 */
@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final ShiftTemplateRepository shiftTemplateRepository;
    private final CourierRepository courierRepository;

    public ShiftService(ShiftRepository shiftRepository,
                       ShiftTemplateRepository shiftTemplateRepository,
                       CourierRepository courierRepository) {
        this.shiftRepository = shiftRepository;
        this.shiftTemplateRepository = shiftTemplateRepository;
        this.courierRepository = courierRepository;
    }

    /**
     * Mevcut tüm vardiya şablonlarını listele
     */
    public List<ShiftTemplateDto> getAvailableShiftTemplates() {
        return shiftTemplateRepository.findByIsActiveTrueOrderByStartTimeAsc()
                .stream()
                .map(this::convertToTemplateDTO)
                .collect(Collectors.toList());
    }

    /**
     * Kurye için vardiya rezerve et
     */
    @Transactional
    public ShiftDto reserveShift(Long courierId, ReserveShiftRequest request) {
        // Kurye kontrolü
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new BusinessException("Kurye bulunamadı"));

        // Şablon kontrolü
        ShiftTemplate template = shiftTemplateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new BusinessException("Vardiya şablonu bulunamadı"));

        if (!template.getIsActive()) {
            throw new BusinessException("Bu vardiya şablonu aktif değil");
        }

        // Tarih kontrolü - geçmiş tarih olamaz
        if (request.getShiftDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Geçmiş tarihli vardiya rezerve edilemez");
        }

        // Başlangıç ve bitiş zamanlarını hesapla
        LocalDateTime startTime = LocalDateTime.of(request.getShiftDate(), template.getStartTime());
        LocalDateTime endTime = LocalDateTime.of(request.getShiftDate(), template.getEndTime());

        // Eğer bitiş zamanı başlangıçtan önce ise (gece vardiyası), bir gün ekle
        if (template.getEndTime().isBefore(template.getStartTime())) {
            endTime = endTime.plusDays(1);
        }

        // Zaman çakışması kontrolü
        if (shiftRepository.hasOverlappingShift(courierId, startTime, endTime)) {
            throw new BusinessException("Bu zaman aralığında zaten bir vardiya rezervasyonunuz var");
        }

        // Yeni vardiya oluştur
        Shift shift = new Shift();
        shift.setCourierId(courierId);
        shift.setStartTime(startTime);
        shift.setEndTime(endTime);
        shift.setShiftRole(template.getDefaultRole());
        shift.setStatus(ShiftStatus.RESERVED);
        shift.setNotes(request.getNotes());

        Shift savedShift = shiftRepository.save(shift);

        ShiftDto dto = convertToDTO(savedShift);
        dto.setCourierName(courier.getName());
        return dto;
    }

    /**
     * Kuryenin gelecek vardiyalarını listele
     */
    public List<ShiftDto> getUpcomingShifts(Long courierId) {
        List<Shift> shifts = shiftRepository.findUpcomingReservedShifts(courierId, LocalDateTime.now());
        return shifts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Kuryenin tüm vardiyalarını listele (filtre ile)
     */
    public List<ShiftDto> getCourierShifts(Long courierId, ShiftStatus status) {
        List<Shift> shifts;
        if (status != null) {
            shifts = shiftRepository.findByCourierIdAndStatusOrderByStartTimeDesc(courierId, status);
        } else {
            shifts = shiftRepository.findByCourierIdAndStartTimeBetween(
                    courierId,
                    LocalDateTime.now().minusMonths(3),
                    LocalDateTime.now().plusMonths(3)
            );
        }
        return shifts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Vardiyaya check-in yap
     */
    @Transactional
    public ShiftDto checkIn(Long courierId, Long shiftId, CheckInRequest request) {
        // Vardiya kontrolü
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new BusinessException("Vardiya bulunamadı"));

        // Kurye kontrolü
        if (!shift.getCourierId().equals(courierId)) {
            throw new BusinessException("Bu vardiya size ait değil");
        }

        // Durum kontrolü
        if (shift.getStatus() != ShiftStatus.RESERVED) {
            throw new BusinessException("Bu vardiyaya zaten giriş yapılmış veya iptal edilmiş");
        }

        // Zaman kontrolü - 30 dakika önceden check-in yapılabilir
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime earliestCheckIn = shift.getStartTime().minusMinutes(30);

        if (now.isBefore(earliestCheckIn)) {
            throw new BusinessException("Vardiyaya giriş için henüz erken. En erken giriş zamanı: " + earliestCheckIn);
        }

        // Check-in işlemi
        shift.setStatus(ShiftStatus.CHECKED_IN);
        shift.setCheckInTime(now);
        if (request.getNotes() != null) {
            shift.setNotes(shift.getNotes() + "\nCheck-in: " + request.getNotes());
        }

        Shift savedShift = shiftRepository.save(shift);

        // Courier'ın on_duty_since alanını güncelle (sıra tabanlı atama için)
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new BusinessException("Kurye bulunamadı"));
        courier.setOnDutySince(now);
        courier.setStatus(Courier.CourierStatus.ONLINE);
        courierRepository.save(courier);

        ShiftDto dto = convertToDTO(savedShift);
        dto.setCourierName(courier.getName());
        return dto;
    }

    /**
     * Vardiyadan check-out yap
     */
    @Transactional
    public ShiftDto checkOut(Long courierId, Long shiftId, CheckOutRequest request) {
        // Vardiya kontrolü
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new BusinessException("Vardiya bulunamadı"));

        // Kurye kontrolü
        if (!shift.getCourierId().equals(courierId)) {
            throw new BusinessException("Bu vardiya size ait değil");
        }

        // Durum kontrolü
        if (shift.getStatus() != ShiftStatus.CHECKED_IN) {
            throw new BusinessException("Bu vardiyaya giriş yapılmamış");
        }

        // Check-out işlemi
        LocalDateTime now = LocalDateTime.now();
        shift.setStatus(ShiftStatus.CHECKED_OUT);
        shift.setCheckOutTime(now);
        if (request.getNotes() != null) {
            shift.setNotes(shift.getNotes() + "\nCheck-out: " + request.getNotes());
        }

        Shift savedShift = shiftRepository.save(shift);

        // Courier'ın on_duty_since alanını temizle
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new BusinessException("Kurye bulunamadı"));
        courier.setOnDutySince(null);
        courier.setStatus(Courier.CourierStatus.OFFLINE);
        courierRepository.save(courier);

        ShiftDto dto = convertToDTO(savedShift);
        dto.setCourierName(courier.getName());
        return dto;
    }

    /**
     * Vardiya rezervasyonunu iptal et
     */
    @Transactional
    public void cancelShift(Long courierId, Long shiftId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new BusinessException("Vardiya bulunamadı"));

        if (!shift.getCourierId().equals(courierId)) {
            throw new BusinessException("Bu vardiya size ait değil");
        }

        if (shift.getStatus() != ShiftStatus.RESERVED) {
            throw new BusinessException("Sadece rezerve edilmiş vardiyalar iptal edilebilir");
        }

        // İptal işlemi - vardiya başlamadan en az 2 saat önce iptal edilebilir
        if (shift.getStartTime().minusHours(2).isBefore(LocalDateTime.now())) {
            throw new BusinessException("Vardiya başlangıcına 2 saatten az kaldı, iptal edilemez");
        }

        shift.setStatus(ShiftStatus.CANCELLED);
        shiftRepository.save(shift);
    }

    /**
     * Kuryenin aktif (CHECKED_IN) vardiyasını getir
     */
    public ShiftDto getActiveShift(Long courierId) {
        return shiftRepository.findByCourierIdAndStatus(courierId, ShiftStatus.CHECKED_IN)
                .map(this::convertToDTO)
                .orElse(null);
    }

    // DTO Dönüşüm metodları
    private ShiftDto convertToDTO(Shift shift) {
        ShiftDto dto = new ShiftDto();
        dto.setShiftId(shift.getShiftId());
        dto.setCourierId(shift.getCourierId());
        dto.setStartTime(shift.getStartTime());
        dto.setEndTime(shift.getEndTime());
        dto.setShiftRole(shift.getShiftRole());
        dto.setStatus(shift.getStatus());
        dto.setCheckInTime(shift.getCheckInTime());
        dto.setCheckOutTime(shift.getCheckOutTime());
        dto.setNotes(shift.getNotes());
        dto.setCreatedAt(shift.getCreatedAt());
        return dto;
    }

    private ShiftTemplateDto convertToTemplateDTO(ShiftTemplate template) {
        ShiftTemplateDto dto = new ShiftTemplateDto();
        dto.setTemplateId(template.getTemplateId());
        dto.setName(template.getName());
        dto.setDescription(template.getDescription());
        dto.setStartTime(template.getStartTime());
        dto.setEndTime(template.getEndTime());
        dto.setDefaultRole(template.getDefaultRole());
        dto.setMaxCouriers(template.getMaxCouriers());
        dto.setIsActive(template.getIsActive());
        return dto;
    }
}

