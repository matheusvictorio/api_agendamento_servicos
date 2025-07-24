package com.neocamp.api_agendamento.service;

import com.neocamp.api_agendamento.domain.dto.request.ClientRequestDTO;
import com.neocamp.api_agendamento.domain.dto.response.ClientResponseDTO;
import com.neocamp.api_agendamento.domain.entities.Address;
import com.neocamp.api_agendamento.domain.entities.Client;
import com.neocamp.api_agendamento.repository.ClientRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ClientService {
    @Autowired
    private ViaCepService viaCepService;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public ClientResponseDTO createClient(@Valid ClientRequestDTO clientRequestDTO) {
        Address address = viaCepService.getAddressByCep(clientRequestDTO.cep(), clientRequestDTO.number(), clientRequestDTO.complement());

        Client client = new Client(
                clientRequestDTO.name(),
                clientRequestDTO.email(),
                passwordEncoder.encode(clientRequestDTO.password()),
                clientRequestDTO.phone(),
                address
                );

        Client savedClient = clientRepository.save(client);
        return toResponseDTO(savedClient);
    }

    private ClientResponseDTO toResponseDTO(Client client) {
        return new ClientResponseDTO(
              client.getId(),
                client.getName(),
                client.getEmail(),
                client.getPhone(),
                client.getActive()
        );
    }
}
