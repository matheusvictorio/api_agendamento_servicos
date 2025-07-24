package com.neocamp.api_agendamento.domain.dto.response;

public record ViaCepResponseDTO(
        String logradouro,
        String bairro,
        String localidade,
        String uf
) {
}
