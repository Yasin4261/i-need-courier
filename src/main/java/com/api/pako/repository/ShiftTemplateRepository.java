package com.api.pako.repository;

import com.api.pako.model.ShiftTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ShiftTemplate entity
 */
@Repository
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long> {

    /**
     * Aktif vardiya şablonlarını getirir
     */
    List<ShiftTemplate> findByIsActiveTrueOrderByStartTimeAsc();

    /**
     * İsme göre vardiya şablonu arar
     */
    List<ShiftTemplate> findByNameContainingIgnoreCase(String name);
}

