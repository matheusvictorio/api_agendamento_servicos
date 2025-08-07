package com.neocamp.api_agendamento.service;

import com.neocamp.api_agendamento.domain.dto.request.ProviderRequestDTO;
import com.neocamp.api_agendamento.domain.dto.request.ProviderUpdateDTO;
import com.neocamp.api_agendamento.domain.dto.response.ProviderResponseDTO;
import com.neocamp.api_agendamento.domain.entities.Address;
import com.neocamp.api_agendamento.domain.entities.Provider;
import com.neocamp.api_agendamento.repository.ProviderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProviderService {
    private final ProviderRepository providerRepository;
    private final ViaCepService viaCepService;
    private final PasswordEncoder passwordEncoder;

    public ProviderService(ProviderRepository providerRepository, ViaCepService viaCepService, PasswordEncoder passwordEncoder) {
        this.providerRepository = providerRepository;
        this.viaCepService = viaCepService;
        this.passwordEncoder = passwordEncoder;
    }

    public ProviderResponseDTO save(ProviderRequestDTO providerRequestDTO) {
        Address address = viaCepService.getAddressByCep(providerRequestDTO.cep(), providerRequestDTO.number(), providerRequestDTO.complement());

        Provider provider = new Provider(
                providerRequestDTO.name(),
                providerRequestDTO.email(),
                passwordEncoder.encode(providerRequestDTO.password()),
                providerRequestDTO.phone(),
                address,
                providerRequestDTO.specialty()
        );

        Provider savedProvider = providerRepository.save(provider);

        return toResponseDTO(savedProvider);


    }

    private ProviderResponseDTO toResponseDTO(Provider savedProvider) {
        return new ProviderResponseDTO(
                savedProvider.getId(),
                savedProvider.getName(),
                savedProvider.getEmail(),
                savedProvider.getPhone(),
                savedProvider.getSpecialty(),
                savedProvider.getActive()
        );
    }

    public ProviderResponseDTO update(Long id, ProviderUpdateDTO providerUpdateDTO) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fornecedor não encontrado!"));

        if (providerUpdateDTO.name() != null) provider.setName(providerUpdateDTO.name());
        if (providerUpdateDTO.phone() != null) provider.setPhone(providerUpdateDTO.phone());
        if (providerUpdateDTO.specialty() != null) provider.setSpecialty(providerUpdateDTO.specialty());
        if (providerUpdateDTO.cep() != null) {
            if (providerUpdateDTO.number() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Para atualizar o CEP, o número do endereço também deve ser informado.");
            }
            Address address = viaCepService.getAddressByCep(providerUpdateDTO.cep(), providerUpdateDTO.number(), providerUpdateDTO.complement());
            provider.setAddress(address);
        }

        Provider savedProvider = providerRepository.save(provider);

        return toResponseDTO(savedProvider);
    }

    public Page<ProviderResponseDTO> findAllProviders(Pageable pageable) {
        return providerRepository.findAllByActiveTrue(pageable)
                .map(this::toResponseDTO);
    }

    public ProviderResponseDTO findProviderById(Long id) {
        Provider provider = providerRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fornecedor não encontrado!"));

        return toResponseDTO(provider);
    }

    public void deleteProvider(Long id) {
        Provider provider = providerRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fornecedor não encontrado!"));

        provider.setActive(false);
        providerRepository.save(provider);
    }

    public ProviderResponseDTO activateProvider(Long id) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fornecedor não encontrado!"));

        provider.setActive(true);
        Provider updated = providerRepository.save(provider);
        return toResponseDTO(updated);
    }

    public ProviderResponseDTO findProviderByEmail(String email) {
        Provider provider = providerRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prestador não encontrado ou inativo!"));
        return toResponseDTO(provider);
    }
}
