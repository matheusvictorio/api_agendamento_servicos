package com.neocamp.api_agendamento.repository;

import com.neocamp.api_agendamento.domain.entities.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
}
