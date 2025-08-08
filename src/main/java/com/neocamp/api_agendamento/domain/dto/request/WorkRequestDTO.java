package com.neocamp.api_agendamento.domain.dto.request;

import com.neocamp.api_agendamento.domain.enums.Specialty;
import jakarta.validation.constraints.NotBlank;

public record WorkRequestDTO(
        @NotBlank
    String name,
    @NotBlank
    String description,
    @NotBlank
    Specialty specialty,
    @NotBlank
    Double price,
    @NotBlank
    Long providerId
) {
}
