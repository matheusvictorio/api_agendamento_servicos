package com.neocamp.api_agendamento.controller;

import com.neocamp.api_agendamento.domain.dto.request.ProviderRequestDTO;
import com.neocamp.api_agendamento.domain.dto.request.ProviderUpdateDTO;
import com.neocamp.api_agendamento.domain.dto.response.ProviderResponseDTO;
import com.neocamp.api_agendamento.service.ProviderService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/providers")
public class ProviderController {
    private final ProviderService providerService;

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @PostMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<ProviderResponseDTO> saveProvider(@RequestBody ProviderRequestDTO providerRequestDTO) {
        ProviderResponseDTO provider = providerService.save(providerRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(provider);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROVIDER') and authentication.principal.id == #id")
    public ResponseEntity<ProviderResponseDTO> updateProvider(@PathVariable Long id, @RequestBody ProviderUpdateDTO providerUpdateDTO) {
        ProviderResponseDTO provider = providerService.update(id, providerUpdateDTO);
        return ResponseEntity.ok().body(provider);
    }

    @GetMapping
    @PreAuthorize("hasRole('CLIENT') or hasRole('PROVIDER')")
    public ResponseEntity<Page<ProviderResponseDTO>> getAllProviders(
            @ParameterObject
            @PageableDefault(size =  10, page = 0)
            Pageable pageable) {
        Page<ProviderResponseDTO> providers = providerService.findAllProviders(pageable);
        return ResponseEntity.ok().body(providers);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT') or (hasRole('PROVIDER') and authentication.principal.id == #id)")
    public ResponseEntity<ProviderResponseDTO> getProviderById(@PathVariable Long id) {
        ProviderResponseDTO provider = providerService.findProviderById(id);
        return ResponseEntity.ok().body(provider);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROVIDER') and authentication.principal.id == #id")
    public ResponseEntity<Void> deleteProvider(@PathVariable Long id) {
        providerService.deleteProvider(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('PROVIDER') and authentication.principal.id == #id")
    public ResponseEntity<ProviderResponseDTO> activateProvider(@PathVariable Long id) {
        ProviderResponseDTO provider = providerService.activateProvider(id);
        return ResponseEntity.ok().body(provider);
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ProviderResponseDTO> getProviderByEmail(@PathVariable String email) {
        ProviderResponseDTO provider = providerService.findProviderByEmail(email);
        return ResponseEntity.ok().body(provider);
    }
}
