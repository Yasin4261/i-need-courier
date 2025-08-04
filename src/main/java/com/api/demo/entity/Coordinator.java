package com.api.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

@Entity
@Table(name = "coordinators")
public class Coordinator {

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

    @Column(name = "employee_id", unique = true)
    private String employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_type", nullable = false)
    private WorkType workType = WorkType.SHIFT;

    @Column(name = "shift_start")
    private LocalTime shiftStart;

    @Column(name = "shift_end")
    private LocalTime shiftEnd;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CoordinatorStatus status = CoordinatorStatus.OFFLINE;

    @Column(name = "current_latitude")
    private Double currentLatitude;

    @Column(name = "current_longitude")
    private Double currentLongitude;

    @Column(name = "coverage_area", length = 500)
    private String coverageArea;

    @Column(name = "max_courier_count")
    private Integer maxCourierCount = 10;

    // İlişkiler
    @OneToMany(mappedBy = "coordinator", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Courier> managedCouriers;

    @OneToMany(mappedBy = "coordinator", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Order> supervisedOrders;

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
    public enum CoordinatorStatus {
        ONLINE, OFFLINE, BUSY, ON_BREAK, IN_FIELD
    }

    public enum WorkType {
        JOKER, SHIFT
    }

    // Constructors
    public Coordinator() {}

    public Coordinator(String name, String email, String phone) {
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

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public WorkType getWorkType() { return workType; }
    public void setWorkType(WorkType workType) { this.workType = workType; }

    public LocalTime getShiftStart() { return shiftStart; }
    public void setShiftStart(LocalTime shiftStart) { this.shiftStart = shiftStart; }

    public LocalTime getShiftEnd() { return shiftEnd; }
    public void setShiftEnd(LocalTime shiftEnd) { this.shiftEnd = shiftEnd; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public CoordinatorStatus getStatus() { return status; }
    public void setStatus(CoordinatorStatus status) { this.status = status; }

    public Double getCurrentLatitude() { return currentLatitude; }
    public void setCurrentLatitude(Double currentLatitude) { this.currentLatitude = currentLatitude; }

    public Double getCurrentLongitude() { return currentLongitude; }
    public void setCurrentLongitude(Double currentLongitude) { this.currentLongitude = currentLongitude; }

    public String getCoverageArea() { return coverageArea; }
    public void setCoverageArea(String coverageArea) { this.coverageArea = coverageArea; }

    public Integer getMaxCourierCount() { return maxCourierCount; }
    public void setMaxCourierCount(Integer maxCourierCount) { this.maxCourierCount = maxCourierCount; }

    public Set<Courier> getManagedCouriers() { return managedCouriers; }
    public void setManagedCouriers(Set<Courier> managedCouriers) { this.managedCouriers = managedCouriers; }

    public Set<Order> getSupervisedOrders() { return supervisedOrders; }
    public void setSupervisedOrders(Set<Order> supervisedOrders) { this.supervisedOrders = supervisedOrders; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
