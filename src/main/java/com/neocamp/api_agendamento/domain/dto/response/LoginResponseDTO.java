package com.neocamp.api_agendamento.domain.dto.response;

public record LoginResponseDTO(
        String token,
        String userType,
        Long userId,
        String name,
        String email
) {
}
