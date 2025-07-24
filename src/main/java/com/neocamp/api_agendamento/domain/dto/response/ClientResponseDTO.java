package com.neocamp.api_agendamento.domain.dto.response;

public record ClientResponseDTO(
        Long id,
        String name,
        String email,
        String phone,
        Boolean active
) {
}
