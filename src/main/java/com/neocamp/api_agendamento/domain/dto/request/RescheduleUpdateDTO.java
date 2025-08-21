package com.neocamp.api_agendamento.domain.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public record RescheduleUpdateDTO(
        @Future(message = "A data deve ser no futuro")
        LocalDateTime date,
        @Pattern(
                regexp = "^\\d{5}-?\\d{3}$",
                message = "CEP deve estar em formato válido, como 12345-678 ou 12345678"
        )
                String cep,

        String number,

        String complement
) {
}
