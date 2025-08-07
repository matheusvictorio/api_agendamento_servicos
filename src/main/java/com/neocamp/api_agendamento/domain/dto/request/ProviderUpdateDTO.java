package com.neocamp.api_agendamento.domain.dto.request;

import com.neocamp.api_agendamento.domain.enums.Specialty;
import jakarta.validation.constraints.Pattern;

public record ProviderUpdateDTO(
        String name,
        @Pattern(
                regexp = "^(\\(\\d{2}\\)\\s?\\d{4,5}-\\d{4}|\\d{10,11})$",
                message = "Telefone deve estar em formato válido, como (11) 91234-5678 ou 11912345678"
        )
        String phone,
        @Pattern(
                regexp = "^\\d{5}-?\\d{3}$",
                message = "CEP deve estar em formato válido, como 12345-678 ou 12345678"
        )
        String cep,
        String number,
        String complement,
        Specialty specialty
) {
}
