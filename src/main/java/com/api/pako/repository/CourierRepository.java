package com.api.pako.repository;

import com.api.pako.model.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Courier entity.
 * Provides database access methods for courier operations.
 */
@Repository
public interface CourierRepository extends JpaRepository<Courier, Long> {

    /**
     * Find courier by email address.
     *
     * @param email Email address
     * @return Optional containing courier if found
     */
    Optional<Courier> findByEmail(String email);

    /**
     * Find courier by phone number.
     *
     * @param phone Phone number
     * @return Optional containing courier if found
     */
    Optional<Courier> findByPhone(String phone);

    /**
     * Check if courier exists with given email.
     *
     * @param email Email address
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Check if courier exists with given phone.
     *
     * @param phone Phone number
     * @return true if exists, false otherwise
     */
    boolean existsByPhone(String phone);
}

