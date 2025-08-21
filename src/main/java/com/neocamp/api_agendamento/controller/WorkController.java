package com.neocamp.api_agendamento.controller;

import com.neocamp.api_agendamento.domain.dto.request.WorkRequestDTO;
import com.neocamp.api_agendamento.domain.dto.request.WorkUpdateDTO;
import com.neocamp.api_agendamento.domain.dto.response.WorkResponseDTO;
import com.neocamp.api_agendamento.domain.enums.Specialty;
import com.neocamp.api_agendamento.service.WorkService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/works")
public class WorkController {
    private final WorkService workService;

    public WorkController(WorkService workService) {
        this.workService = workService;
    }

    @PostMapping
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<WorkResponseDTO> save(@RequestBody WorkRequestDTO workRequestDTO) {
        WorkResponseDTO workResponseDTO = workService.saveWork(workRequestDTO);
        return ResponseEntity.ok().body(workResponseDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<WorkResponseDTO> update(@PathVariable Long id, @RequestBody WorkUpdateDTO workUpdateDTO) {
        WorkResponseDTO workResponseDTO = workService.updateWork(id, workUpdateDTO);
        return ResponseEntity.ok().body(workResponseDTO);
    }

    @GetMapping
    @PreAuthorize("hasRole('CLIENT') or hasRole('PROVIDER')")
    public ResponseEntity<Page<WorkResponseDTO>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Specialty specialty,
            @RequestParam(required = false) Long providerId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @ParameterObject
            @PageableDefault(size =  10, page = 0)
            Pageable pageable) {
        Page<WorkResponseDTO> works = workService.searchWorks(name, specialty, providerId, minPrice, maxPrice, pageable);
        return ResponseEntity.ok().body(works);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PROVIDER')")
    public ResponseEntity<WorkResponseDTO> findById(@PathVariable Long id) {
        WorkResponseDTO workResponseDTO = workService.findWorkById(id);
        return ResponseEntity.ok().body(workResponseDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workService.deleteWork(id);
        return ResponseEntity.noContent().build();
    }

}
