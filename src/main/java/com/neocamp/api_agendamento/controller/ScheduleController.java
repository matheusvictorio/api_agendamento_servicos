package com.neocamp.api_agendamento.controller;

import com.neocamp.api_agendamento.domain.dto.request.RescheduleUpdateDTO;
import com.neocamp.api_agendamento.domain.dto.request.ScheduleRequestDTO;
import com.neocamp.api_agendamento.domain.dto.response.ScheduleResponseDTO;
import com.neocamp.api_agendamento.service.ScheduleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schedules")
public class ScheduleController {
    private final ScheduleService scheduleService;
    public ScheduleController(ScheduleService scheduleService){
        this.scheduleService = scheduleService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ScheduleResponseDTO> saveSchedule(@RequestBody ScheduleRequestDTO scheduleRequestDTO){
        ScheduleResponseDTO scheduleResponseDTO = scheduleService.saveSchedule(scheduleRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleResponseDTO);
    }

    @GetMapping("/{id}/confirm")
    public ResponseEntity<String>  confirmSchedule(@PathVariable Long id) {
        String confirmationMessage = scheduleService.confirmSchedule(id);
        return ResponseEntity.ok().body(confirmationMessage);
    }

    //para api
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelSchedule(@PathVariable Long id) {
        scheduleService.cancelSchedule(id);
        return ResponseEntity.noContent().build();
    }

    //para email
    @GetMapping("/{id}/cancel")
    public ResponseEntity<String> cancelScheduleFromEmail(@PathVariable Long id) {
        scheduleService.cancelSchedule(id);
        return ResponseEntity.ok("✅ O agendamento foi cancelado com sucesso!");
    }


    @GetMapping("/rate")
    public ResponseEntity<Void> submitRating(
            @RequestParam Long scheduleId,
            @RequestParam String type,
            @RequestParam Double rating
    ) {
        scheduleService.submitRating(scheduleId, type, rating);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/finalize")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<String> finalizeSchedule(@PathVariable Long id) {
        String message = scheduleService.finalizeSchedule(id);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PROVIDER')")
    public ResponseEntity<ScheduleResponseDTO> reschedule(@RequestBody RescheduleUpdateDTO rescheduleUpdateDTO, @PathVariable Long id) {
        ScheduleResponseDTO scheduleResponseDTO = scheduleService.rescheduleSchedule(id, rescheduleUpdateDTO);
        return ResponseEntity.ok().body(scheduleResponseDTO);
    }

    @GetMapping("/client")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<ScheduleResponseDTO>> getClientSchedules() {
        List<ScheduleResponseDTO> scheduleResponseDTO =  scheduleService.getSchedulesByClient();
        return ResponseEntity.ok().body(scheduleResponseDTO);
    }

    @GetMapping("/provider")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<?> getProviderSchedules() {
        return ResponseEntity.ok(scheduleService.getSchedulesByProvider());
    }


    @GetMapping("/provider/{providerId}/available")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PROVIDER')")
    public ResponseEntity<?> getProviderAvailableSlots(@PathVariable Long providerId) {
        return ResponseEntity.ok(scheduleService.getProviderAvailableSlots(providerId));
    }
}
