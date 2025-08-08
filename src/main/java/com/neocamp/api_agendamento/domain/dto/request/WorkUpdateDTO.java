package com.neocamp.api_agendamento.domain.dto.request;

import com.neocamp.api_agendamento.domain.enums.Specialty;

public record WorkUpdateDTO(
        String name,
        String description,
        Specialty specialty,
        Double price,
        Long providerId
) {
}
