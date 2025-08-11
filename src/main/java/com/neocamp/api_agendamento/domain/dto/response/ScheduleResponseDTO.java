package com.neocamp.api_agendamento.domain.dto.response;

import com.neocamp.api_agendamento.domain.entities.Address;
import com.neocamp.api_agendamento.domain.enums.Specialty;
import com.neocamp.api_agendamento.domain.enums.Status;

import java.time.LocalDateTime;

public record ScheduleResponseDTO(
    Long id,
    String clientName,
    String clientEmail,
    String clientPhone,
    String providerName,
    String providerEmail,
    String providerPhone,
    String workName,
    Specialty workSpecialty,
    Double workPrice,
    LocalDateTime dateTime,
    Status status,
    Address address
) {
}
