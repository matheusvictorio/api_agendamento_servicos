package com.neocamp.api_agendamento.domain.dto.response;

public record RegisterResponseDTO(
        Long id,
        String name,
        String email,
        String userType,
        String message
) {
}
