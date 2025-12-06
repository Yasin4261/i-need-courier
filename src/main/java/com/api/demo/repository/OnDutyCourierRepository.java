package com.api.demo.repository;

import com.api.demo.model.OnDutyCourier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OnDutyCourierRepository extends JpaRepository<OnDutyCourier, Long> {

    Optional<OnDutyCourier> findByCourierId(Long courierId);

    List<OnDutyCourier> findAllByOrderByOnDutySinceAsc();

    void deleteByCourierId(Long courierId);

    boolean existsByCourierId(Long courierId);
}

