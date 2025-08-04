package com.api.demo.controller;

import com.api.demo.dto.CourierDTO;
import com.api.demo.service.CourierService;
import com.api.demo.service.CourierQueueService;
import com.api.demo.service.CourierEventService;
import com.api.demo.service.OrderAssignmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/couriers")
@CrossOrigin(origins = "*")
public class CourierController {

    private final CourierService courierService;
    private final CourierQueueService courierQueueService;
    private final CourierEventService courierEventService;
    private final OrderAssignmentService orderAssignmentService;

    public CourierController(
            CourierService courierService,
            CourierQueueService courierQueueService,
            CourierEventService courierEventService,
            OrderAssignmentService orderAssignmentService) {
        this.courierService = courierService;
        this.courierQueueService = courierQueueService;
        this.courierEventService = courierEventService;
        this.orderAssignmentService = orderAssignmentService;
    }

    // Existing endpoints...
    @GetMapping
    public ResponseEntity<List<CourierDTO>> getAllCouriers() {
        List<CourierDTO> couriers = courierService.getAllCouriers();
        return ResponseEntity.ok(couriers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourierDTO> getCourierById(@PathVariable Long id) {
        Optional<CourierDTO> courier = courierService.getCourierById(id);
        return courier.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CourierDTO> createCourier(@Valid @RequestBody CourierDTO courierDTO) {
        CourierDTO createdCourier = courierService.createCourier(courierDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCourier);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourierDTO> updateCourier(@PathVariable Long id,
                                                   @Valid @RequestBody CourierDTO courierDTO) {
        Optional<CourierDTO> updatedCourier = courierService.updateCourier(id, courierDTO);
        return updatedCourier.map(ResponseEntity::ok)
                            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourier(@PathVariable Long id) {
        boolean deleted = courierService.deleteCourier(id);
        return deleted ? ResponseEntity.noContent().build()
                      : ResponseEntity.notFound().build();
    }

    // ===== YENİ VARDİYA VE SIRA YÖNETİMİ ENDPOİNTLERİ =====

    /**
     * Kurye vardiya başlatır
     */
    @PostMapping("/{id}/start-shift")
    public ResponseEntity<Map<String, Object>> startShift(@PathVariable Long id) {
        try {
            courierEventService.startCourierShift(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Shift started successfully",
                "courierId", id,
                "queuePosition", courierQueueService.getCourierQueue().indexOf(id)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to start shift: " + e.getMessage()
            ));
        }
    }

    /**
     * Kurye vardiya bitirir
     */
    @PostMapping("/{id}/end-shift")
    public ResponseEntity<Map<String, Object>> endShift(@PathVariable Long id) {
        try {
            courierEventService.endCourierShift(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Shift ended successfully",
                "courierId", id
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to end shift: " + e.getMessage()
            ));
        }
    }

    /**
     * Kurye lokasyon günceller
     */
    @PostMapping("/{id}/update-location")
    public ResponseEntity<Map<String, Object>> updateLocation(
            @PathVariable Long id,
            @RequestBody Map<String, Double> location) {
        try {
            Double latitude = location.get("latitude");
            Double longitude = location.get("longitude");

            if (latitude == null || longitude == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Latitude and longitude are required"
                ));
            }

            courierEventService.updateCourierLocation(id, latitude, longitude);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Location updated successfully",
                "courierId", id,
                "latitude", latitude,
                "longitude", longitude
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to update location: " + e.getMessage()
            ));
        }
    }

    /**
     * Kurye durumu günceller (AVAILABLE, BUSY, ON_BREAK)
     */
    @PostMapping("/{id}/update-status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {
        try {
            String newStatus = statusUpdate.get("status");
            if (newStatus == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Status is required"
                ));
            }

            courierQueueService.updateCourierStatus(id, newStatus);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Status updated successfully",
                "courierId", id,
                "status", newStatus
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to update status: " + e.getMessage()
            ));
        }
    }

    /**
     * Aktif kurye sırasını getirir
     */
    @GetMapping("/queue")
    public ResponseEntity<Map<String, Object>> getCourierQueue() {
        try {
            List<Object> queue = courierQueueService.getCourierQueue();
            CourierQueueService.QueueStatistics stats = courierQueueService.getQueueStatistics();

            return ResponseEntity.ok(Map.of(
                "queue", queue,
                "statistics", Map.of(
                    "totalCouriers", stats.getTotalCouriers(),
                    "availableCouriers", stats.getAvailableCouriers(),
                    "currentIndex", stats.getCurrentIndex()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to get queue: " + e.getMessage()
            ));
        }
    }

    /**
     * Sıradaki sıra numarasını getirir
     */
    @GetMapping("/{id}/queue-position")
    public ResponseEntity<Map<String, Object>> getQueuePosition(@PathVariable Long id) {
        try {
            List<Object> queue = courierQueueService.getCourierQueue();
            int position = queue.indexOf(id);
            boolean inQueue = courierQueueService.isCourierInQueue(id);
            boolean available = courierQueueService.isCourierAvailable(id);

            return ResponseEntity.ok(Map.of(
                "courierId", id,
                "inQueue", inQueue,
                "position", position >= 0 ? position : -1,
                "isAvailable", available,
                "queueSize", queue.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to get queue position: " + e.getMessage()
            ));
        }
    }

    /**
     * Sıradaki sırada bekleyen kuryeyi getirir (test amaçlı)
     */
    @GetMapping("/next-available")
    public ResponseEntity<Map<String, Object>> getNextAvailableCourier() {
        try {
            Long nextCourierId = courierQueueService.getNextAvailableCourier();

            if (nextCourierId != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "nextCourierId", nextCourierId,
                    "message", "Next available courier found"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "No available courier in queue"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to get next courier: " + e.getMessage()
            ));
        }
    }

    /**
     * Toplu optimizasyon tetikler (admin için)
     */
    @PostMapping("/optimize-assignments")
    public ResponseEntity<Map<String, Object>> optimizeAssignments() {
        try {
            orderAssignmentService.optimizeBulkAssignments();

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Bulk optimization triggered successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to trigger optimization: " + e.getMessage()
            ));
        }
    }

    // Existing methods continue...
    @GetMapping("/available")
    public ResponseEntity<List<CourierDTO>> getAvailableCouriers() {
        List<CourierDTO> availableCouriers = courierService.getAvailableCouriers();
        return ResponseEntity.ok(availableCouriers);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CourierDTO>> searchCouriers(@RequestParam String name) {
        List<CourierDTO> couriers = courierService.searchCouriersByName(name);
        return ResponseEntity.ok(couriers);
    }

    @GetMapping("/count/available")
    public ResponseEntity<Long> countAvailableCouriers() {
        Long count = courierService.countAvailableCouriers();
        return ResponseEntity.ok(count);
    }
}
