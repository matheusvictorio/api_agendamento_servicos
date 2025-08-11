package com.neocamp.api_agendamento.controller;

import com.neocamp.api_agendamento.domain.dto.request.ScheduleRequestDTO;
import com.neocamp.api_agendamento.domain.dto.response.ScheduleResponseDTO;
import com.neocamp.api_agendamento.service.ScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/schedules")
public class ScheduleController {
    private final ScheduleService scheduleService;
    public ScheduleController(ScheduleService scheduleService){
        this.scheduleService = scheduleService;
    }

    @PostMapping
    public ResponseEntity<ScheduleResponseDTO> saveSchedule(@RequestBody ScheduleRequestDTO scheduleRequestDTO){
        ScheduleResponseDTO scheduleResponseDTO = scheduleService.saveSchedule(scheduleRequestDTO);
        return ResponseEntity.ok().body(scheduleResponseDTO);
    }

    @GetMapping("/{id}/confirm")
    public ResponseEntity<String>  confirmSchedule(@PathVariable Long id) {
        String confirmationMessage = scheduleService.confirmSchedule(id);
        return ResponseEntity.ok().body(confirmationMessage);
    }

    @GetMapping("/{id}/cancel")
    public ResponseEntity<String> cancelSchedule(@PathVariable Long id) {
        String cancellationMessage = scheduleService.cancelSchedule(id);
        return ResponseEntity.ok().body(cancellationMessage);
    }
}
