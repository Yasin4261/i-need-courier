package com.api.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

@Entity
@Table(name = "couriers")
public class Courier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "system_user_id", unique = true)
    private SystemUser systemUser;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "license_number")
    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type")
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_type", nullable = false)
    private WorkType workType = WorkType.SHIFT;

    @Column(name = "shift_start")
    private LocalTime shiftStart;

    @Column(name = "shift_end")
    private LocalTime shiftEnd;

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CourierStatus status = CourierStatus.OFFLINE;

    @Column(name = "current_latitude")
    private Double currentLatitude;

    @Column(name = "current_longitude")
    private Double currentLongitude;

    @Column(name = "current_location_name")
    private String currentLocationName;

    @Column(name = "total_deliveries")
    private Integer totalDeliveries = 0;

    // Koordinatör ilişkisi
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordinator_id")
    private Coordinator coordinator;

    // Relationships
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @OneToMany(mappedBy = "courier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Order> orders;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Enums
    public enum CourierStatus {
        ONLINE, OFFLINE, BUSY, ON_BREAK, IN_DELIVERY
    }

    public enum WorkType {
        JOKER,    // Esnek çalışma
        SHIFT     // Vardiyalı çalışma
    }

    public enum VehicleType {
        BICYCLE, MOTORCYCLE, CAR, VAN, TRUCK, WALKING
    }

    // Constructors
    public Courier() {}

    public Courier(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SystemUser getSystemUser() { return systemUser; }
    public void setSystemUser(SystemUser systemUser) { this.systemUser = systemUser; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }

    public WorkType getWorkType() { return workType; }
    public void setWorkType(WorkType workType) { this.workType = workType; }

    public LocalTime getShiftStart() { return shiftStart; }
    public void setShiftStart(LocalTime shiftStart) { this.shiftStart = shiftStart; }

    public LocalTime getShiftEnd() { return shiftEnd; }
    public void setShiftEnd(LocalTime shiftEnd) { this.shiftEnd = shiftEnd; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }

    public CourierStatus getStatus() { return status; }
    public void setStatus(CourierStatus status) { this.status = status; }

    public Double getCurrentLatitude() { return currentLatitude; }
    public void setCurrentLatitude(Double currentLatitude) { this.currentLatitude = currentLatitude; }

    public Double getCurrentLongitude() { return currentLongitude; }
    public void setCurrentLongitude(Double currentLongitude) { this.currentLongitude = currentLongitude; }

    public String getCurrentLocationName() { return currentLocationName; }
    public void setCurrentLocationName(String currentLocationName) { this.currentLocationName = currentLocationName; }

    public Integer getTotalDeliveries() { return totalDeliveries; }
    public void setTotalDeliveries(Integer totalDeliveries) { this.totalDeliveries = totalDeliveries; }

    public Coordinator getCoordinator() { return coordinator; }
    public void setCoordinator(Coordinator coordinator) { this.coordinator = coordinator; }

    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    public Set<Order> getOrders() { return orders; }
    public void setOrders(Set<Order> orders) { this.orders = orders; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
