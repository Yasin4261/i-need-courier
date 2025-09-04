package com.api.demo.service;

import com.api.demo.dto.CourierDTO;
import com.api.demo.entity.Courier;
import com.api.demo.repository.CourierRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CourierService {
    
    private final CourierRepository courierRepository;
    
    public CourierService(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    // Get all couriers
    public List<CourierDTO> getAllCouriers() {
        return courierRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get courier by ID
    public Optional<CourierDTO> getCourierById(Long id) {
        return courierRepository.findById(id)
                .map(this::convertToDTO);
    }

    // Create new courier
    public CourierDTO createCourier(CourierDTO courierDTO) {
        Courier courier = convertToEntity(courierDTO);
        Courier savedCourier = courierRepository.save(courier);
        return convertToDTO(savedCourier);
    }

    // Update courier
    public Optional<CourierDTO> updateCourier(Long id, CourierDTO courierDTO) {
        return courierRepository.findById(id)
                .map(existingCourier -> {
                    existingCourier.setName(courierDTO.getName());
                    existingCourier.setEmail(courierDTO.getEmail());
                    existingCourier.setPhone(courierDTO.getPhone());
                    existingCourier.setIsAvailable(courierDTO.getIsAvailable());
                    return convertToDTO(courierRepository.save(existingCourier));
                });
    }

    // Delete courier
    public boolean deleteCourier(Long id) {
        if (courierRepository.existsById(id)) {
            courierRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Get available couriers
    public List<CourierDTO> getAvailableCouriers() {
        return courierRepository.findByIsAvailable(true)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Search couriers by name
    public List<CourierDTO> searchCouriersByName(String name) {
        return courierRepository.findByNameContaining(name)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Count available couriers
    public Long countAvailableCouriers() {
        return courierRepository.countAvailableCouriers();
    }

    // Convert Entity to DTO
    private CourierDTO convertToDTO(Courier courier) {
        CourierDTO dto = new CourierDTO();
        dto.setId(courier.getId());
        dto.setName(courier.getName());
        dto.setEmail(courier.getEmail());
        dto.setPhone(courier.getPhone());
        dto.setIsAvailable(courier.getIsAvailable());
        return dto;
    }

    // Convert DTO to Entity
    private Courier convertToEntity(CourierDTO dto) {
        Courier courier = new Courier();
        courier.setName(dto.getName());
        courier.setEmail(dto.getEmail());
        courier.setPhone(dto.getPhone());
        courier.setIsAvailable(dto.getIsAvailable() != null ? dto.getIsAvailable() : true);
        return courier;
    }
}
