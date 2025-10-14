package com.api.demo.domain.port.output;

import com.api.demo.domain.model.Courier;
import com.api.demo.domain.valueobject.Email;

import java.util.Optional;

public interface CourierRepository {
    Optional<Courier> findById(Long id);
    Optional<Courier> findByEmail(Email email);
    Courier save(Courier courier);
    boolean existsByEmail(Email email);
    void updateLastLogin(Long courierId);
}
