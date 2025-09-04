package com.api.demo.repository;

import com.api.demo.entity.Courier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CourierRepositoryTest {
    @Autowired
    private CourierRepository courierRepository;

    @Test
    void saveAndFindCourier() {
        Courier courier = new Courier();
        courier.setName("Test Courier");
        courier.setEmail("test@courier.com");
        courier.setPhone("1234567890");
        courier.setIsAvailable(true);
        Courier saved = courierRepository.save(courier);
        Optional<Courier> found = courierRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Test Courier", found.get().getName());
    }
}

