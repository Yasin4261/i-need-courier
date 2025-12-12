package com.api.demo.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "on_duty_couriers")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OnDutyCourier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "courier_id", nullable = false, unique = true)
    private Long courierId;

    @Column(name = "shift_id")
    private Long shiftId;

    @Column(name = "on_duty_since", nullable = false)
    private OffsetDateTime onDutySince;

    @Column(name = "source", nullable = false)
    private String source = "app";

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

