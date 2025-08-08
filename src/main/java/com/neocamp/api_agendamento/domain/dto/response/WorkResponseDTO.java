package com.neocamp.api_agendamento.domain.dto.response;

import com.neocamp.api_agendamento.domain.enums.Specialty;

public record WorkResponseDTO (
    Long id,
    String name,
    String description,
    Specialty specialty,
    Double price,
    String providerName,
    String providerEmail,
    String providerPhone
) {
}
