package com.api.demo.repository;

import com.api.demo.entity.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourierRepository extends JpaRepository<Courier, Long> {

    // Basic CRUD operations are inherited from JpaRepository

    // Custom query methods
    Optional<Courier> findByEmail(String email);

    List<Courier> findByIsAvailable(Boolean isAvailable);

    @Query("SELECT c FROM Courier c WHERE c.name LIKE %?1%")
    List<Courier> findByNameContaining(String name);

    @Query("SELECT COUNT(c) FROM Courier c WHERE c.isAvailable = true")
    Long countAvailableCouriers();
}
