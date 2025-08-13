package com.neocamp.api_agendamento.domain.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public record ScheduleRequestDTO(
        @NotBlank
        Long clientId,
        @NotBlank
        Long providerId,
        @NotBlank
        Long workId,
        @NotBlank
        @Pattern(
                regexp = "^\\d{5}-?\\d{3}$",
                message = "CEP deve estar em formato válido, como 12345-678 ou 12345678"
        )
        String cep,

        @Future(message = "A data deve ser no futuro")
        LocalDateTime date,

        @NotBlank
        String number,

        String complement
) {
}
