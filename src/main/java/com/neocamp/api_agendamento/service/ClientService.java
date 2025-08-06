package com.neocamp.api_agendamento.service;

import com.neocamp.api_agendamento.domain.dto.request.ClientRequestDTO;
import com.neocamp.api_agendamento.domain.dto.request.ClientUpdateDTO;
import com.neocamp.api_agendamento.domain.dto.response.ClientResponseDTO;
import com.neocamp.api_agendamento.domain.entities.Address;
import com.neocamp.api_agendamento.domain.entities.Client;
import com.neocamp.api_agendamento.repository.ClientRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public ClientResponseDTO updateClient(Long id, @Valid ClientUpdateDTO clientUpdateDTO) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado!"));

        if (clientUpdateDTO.name() != null) client.setName(clientUpdateDTO.name());
        if (clientUpdateDTO.phone() != null) client.setPhone(clientUpdateDTO.phone());

        if (clientUpdateDTO.cep() != null && clientUpdateDTO.number() != null) {
            Address address = viaCepService.getAddressByCep(clientUpdateDTO.cep(), clientUpdateDTO.number(), clientUpdateDTO.complement());
            client.setAddress(address);
        }

        Client updated = clientRepository.save(client);
        return toResponseDTO(updated);
    }

    public Page<ClientResponseDTO> findAllClients(Pageable pageable) {
        return clientRepository.findAll(pageable).map(this::toResponseDTO);
    }

    public ClientResponseDTO findClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado!"));
        return toResponseDTO(client);
    }
}
