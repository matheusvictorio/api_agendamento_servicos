package com.neocamp.api_agendamento.service;

import com.neocamp.api_agendamento.domain.dto.request.WorkRequestDTO;
import com.neocamp.api_agendamento.domain.dto.request.WorkUpdateDTO;
import com.neocamp.api_agendamento.domain.dto.response.WorkResponseDTO;
import com.neocamp.api_agendamento.domain.entities.Provider;
import com.neocamp.api_agendamento.domain.entities.Work;
import com.neocamp.api_agendamento.domain.enums.Specialty;
import com.neocamp.api_agendamento.repository.ProviderRepository;
import com.neocamp.api_agendamento.repository.WorkRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WorkService {
    private final WorkRepository workRepository;
    private final ProviderRepository providerRepository;

    public WorkService(WorkRepository workRepository, ProviderRepository providerRepository) {
        this.workRepository = workRepository;
        this.providerRepository = providerRepository;
    }

    public WorkResponseDTO saveWork(WorkRequestDTO workRequestDTO) {
        Work work = new Work();
        work.setName(workRequestDTO.name());
        work.setDescription(workRequestDTO.description());
        work.setSpecialty(workRequestDTO.specialty());
        work.setPrice(workRequestDTO.price());
        Provider provider = providerRepository.findById(workRequestDTO.providerId()).orElse(null);
        work.setProvider(provider);
        workRepository.save(work);

        return toResponseDTO(work);
    }

    public WorkResponseDTO toResponseDTO(Work work) {
        return new WorkResponseDTO(
                work.getId(),
                work.getName(),
                work.getDescription(),
                work.getSpecialty(),
                work.getPrice(),
                work.getProvider().getName(),
                work.getProvider().getEmail(),
                work.getProvider().getPhone()
        );
    }

    public WorkResponseDTO updateWork(Long id, WorkUpdateDTO workUpdateDTO) {
        Work work = workRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Work not found"));

        if (workUpdateDTO.name() != null) work.setName(workUpdateDTO.name());
        if (workUpdateDTO.description() != null) work.setDescription(workUpdateDTO.description());
        if (workUpdateDTO.specialty() != null) work.setSpecialty(workUpdateDTO.specialty());
        if (workUpdateDTO.price() != null) work.setPrice(workUpdateDTO.price());
        if (workUpdateDTO.providerId() != null) {
            Provider provider = providerRepository.findById(workUpdateDTO.providerId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prestador não encontrado"));
            work.setProvider(provider);
        }

        workRepository.save(work);
        return toResponseDTO(work);
    }

    public WorkResponseDTO findWorkById(Long id) {
        Work work = workRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trabalho não encontrado"));
        return toResponseDTO(work);
    }


    public Page<WorkResponseDTO> searchWorks(String name, Specialty specialty, Long providerId, Double minPrice, Double maxPrice, Pageable pageable) {
        Page<Work> works = workRepository.findWithFilter(name, specialty, providerId, minPrice, maxPrice, pageable);
        return works.map(this::toResponseDTO);
    }

    public void deleteWork(Long id) {
        Work work = workRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trabalho não encontrado"));
        workRepository.delete(work);
    }
}
