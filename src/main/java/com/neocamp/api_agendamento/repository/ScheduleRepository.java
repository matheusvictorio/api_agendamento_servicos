package com.neocamp.api_agendamento.repository;

import com.neocamp.api_agendamento.domain.entities.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    boolean existsByClientIdAndDateTime(Long clientId, LocalDateTime dateTime);
    boolean existsByProviderIdAndDateTime(Long providerId, LocalDateTime dateTime);

    long countByClientIdAndDateTimeBetweenAndStatusNot(Long clientId, LocalDateTime start, LocalDateTime end, com.neocamp.api_agendamento.domain.enums.Status status);
    long countByProviderIdAndDateTimeBetweenAndStatusNot(Long providerId, LocalDateTime start, LocalDateTime end, com.neocamp.api_agendamento.domain.enums.Status status);

    long countByClientIdAndStatusNot(Long clientId, com.neocamp.api_agendamento.domain.enums.Status status);
    long countByProviderIdAndStatusNot(Long providerId, com.neocamp.api_agendamento.domain.enums.Status status);

    List<Schedule> findByDateTimeBetweenAndStatus(LocalDateTime start, LocalDateTime end, com.neocamp.api_agendamento.domain.enums.Status status);
    
    List<Schedule> findByClientId(Long clientId);

    List<Schedule> findByProviderId(Long providerId);
}
