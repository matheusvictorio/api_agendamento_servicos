package com.neocamp.api_agendamento.service;

import com.neocamp.api_agendamento.domain.dto.response.ViaCepResponseDTO;
import com.neocamp.api_agendamento.domain.entities.Address;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ViaCepService {
    private final RestTemplate restTemplate = new RestTemplate();
    public Address getAddressByCep(String cep, String number, String complement) {
        var url = "https://viacep.com.br/ws/" + cep + "/json/";
        var response = restTemplate.getForObject(url, ViaCepResponseDTO.class);

        if (response.getClass() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CEP não encontrado");
        }

        return new Address(
                response.logradouro(),
                number,
                complement,
                response.bairro(),
                response.localidade(),
                response.uf(),
                cep
        );
    }
}
