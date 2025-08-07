package com.neocamp.api_agendamento.repository;

import com.neocamp.api_agendamento.domain.entities.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Page<Client> findAllByActiveTrue(Pageable pageable);
    java.util.Optional<Client> findByIdAndActiveTrue(Long id);

    Optional<Client> findByEmailAndActiveTrue(String email);
}
