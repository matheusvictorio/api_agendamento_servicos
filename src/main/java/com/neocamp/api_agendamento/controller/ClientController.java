package com.neocamp.api_agendamento.controller;

import com.neocamp.api_agendamento.domain.dto.request.ClientRequestDTO;
import com.neocamp.api_agendamento.domain.dto.request.ClientUpdateDTO;
import com.neocamp.api_agendamento.domain.dto.response.ClientResponseDTO;
import com.neocamp.api_agendamento.service.ClientService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clients")
public class ClientController {
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<ClientResponseDTO> createClient(@RequestBody @Valid ClientRequestDTO clientRequestDTO) {
        ClientResponseDTO clientResponseDTO = clientService.createClient(clientRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(clientResponseDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT') and authentication.principal.id == #id")
    public ResponseEntity<ClientResponseDTO> updateClient(@PathVariable Long id,@RequestBody @Valid ClientUpdateDTO clientUpdateDTO) {
        ClientResponseDTO clientResponseDTO = clientService.updateClient(id, clientUpdateDTO);
        return ResponseEntity.ok().body(clientResponseDTO);
    }

    @GetMapping
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Page<ClientResponseDTO>> getAllClients(
            @ParameterObject
            @PageableDefault(size =  10, page = 0)
            Pageable pageable) {
        Page<ClientResponseDTO> clients = clientService.findAllClients(pageable);
        return ResponseEntity.ok().body(clients);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PROVIDER') or (hasRole('CLIENT') and authentication.principal.id == #id)")
    public ResponseEntity<ClientResponseDTO> getClientById(@PathVariable Long id) {
        ClientResponseDTO client = clientService.findClientById(id);
        return ResponseEntity.ok().body(client);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT') and authentication.principal.id == #id")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('CLIENT') and authentication.principal.id == #id")
    public ResponseEntity<ClientResponseDTO> activateClient(@PathVariable Long id) {
        ClientResponseDTO clientResponseDTO = clientService.activateClient(id);
        return ResponseEntity.ok().body(clientResponseDTO);
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ClientResponseDTO> getClientByEmail(@PathVariable String email) {
        ClientResponseDTO client = clientService.findClientByEmail(email);
        return ResponseEntity.ok().body(client);
    }
}
