package com.neocamp.api_agendamento.repository;

import com.neocamp.api_agendamento.domain.entities.Provider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProviderRepository extends JpaRepository<Provider, Long> {
   Page<Provider> findAllByActiveTrue(Pageable pageable);

  Optional<Provider> findByIdAndActiveTrue(Long id);

  Optional<Provider> findByEmailAndActiveTrue(String email);
}
