package com.neocamp.api_agendamento.repository;

import com.neocamp.api_agendamento.domain.dto.response.WorkResponseDTO;
import com.neocamp.api_agendamento.domain.entities.Work;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WorkRepository extends JpaRepository<Work, Long>, JpaSpecificationExecutor<Work> {
    @Query("""
        SELECT w FROM Work w
        WHERE (:name IS NULL OR LOWER(w.name) LIKE LOWER(CONCAT('%', :name, '%')))
        AND (:specialty IS NULL OR w.specialty = :specialty)
        AND (:providerId IS NULL OR w.provider.id = :providerId)
        AND (:minPrice IS NULL OR w.price >= :minPrice)
        AND (:maxPrice IS NULL OR w.price <= :maxPrice)
    """)
    Page<Work> findWithFilter(@Param("name") String name,
                             @Param("specialty") com.neocamp.api_agendamento.domain.enums.Specialty specialty,
                             @Param("providerId") Long providerId,
                             @Param("minPrice") Double minPrice,
                             @Param("maxPrice") Double maxPrice,
                             Pageable pageable);
}
