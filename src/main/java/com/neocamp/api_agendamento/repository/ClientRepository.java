package com.neocamp.api_agendamento.repository;

import com.neocamp.api_agendamento.domain.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
}
