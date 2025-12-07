package com.api.demo.repository;

import com.api.demo.model.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Business entity.
 * Provides database access methods for business operations.
 */
@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {

    /**
     * Find business by email address.
     *
     * @param email Email address
     * @return Optional containing business if found
     */
    Optional<Business> findByEmail(String email);

    /**
     * Find business by business code.
     *
     * @param businessCode Business code
     * @return Optional containing business if found
     */
    Optional<Business> findByBusinessCode(String businessCode);

    /**
     * Check if business exists with given email.
     *
     * @param email Email address
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Check if business exists with given business code.
     *
     * @param businessCode Business code
     * @return true if exists, false otherwise
     */
    boolean existsByBusinessCode(String businessCode);

    /**
     * Check if business exists with given phone.
     *
     * @param phone Phone number
     * @return true if exists, false otherwise
     */
    boolean existsByPhone(String phone);
}

