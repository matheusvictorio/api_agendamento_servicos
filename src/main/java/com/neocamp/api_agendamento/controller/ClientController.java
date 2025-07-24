package com.neocamp.api_agendamento.controller;

import com.neocamp.api_agendamento.domain.dto.request.ClientRequestDTO;
import com.neocamp.api_agendamento.domain.dto.response.ClientResponseDTO;
import com.neocamp.api_agendamento.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clients")
public class ClientController {
    @Autowired
    private ClientService clientService;

    @PostMapping
    public ResponseEntity<ClientResponseDTO> createClient(@RequestBody @Valid ClientRequestDTO clientRequestDTO) {
        ClientResponseDTO clientResponseDTO = clientService.createClient(clientRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(clientResponseDTO);
    }
}
