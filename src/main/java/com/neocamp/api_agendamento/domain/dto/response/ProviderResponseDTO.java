package com.neocamp.api_agendamento.domain.dto.response;

import com.neocamp.api_agendamento.domain.enums.Specialty;

public record ProviderResponseDTO(
        Long id,
        String name,
        String email,
        String phone,
        Specialty specialty,
        Boolean active
){
}
