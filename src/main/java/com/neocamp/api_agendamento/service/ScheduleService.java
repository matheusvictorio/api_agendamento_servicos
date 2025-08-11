package com.neocamp.api_agendamento.service;

import com.neocamp.api_agendamento.domain.dto.request.ScheduleRequestDTO;
import com.neocamp.api_agendamento.domain.dto.response.ScheduleResponseDTO;
import com.neocamp.api_agendamento.domain.entities.Schedule;
import com.neocamp.api_agendamento.domain.enums.Status;
import com.neocamp.api_agendamento.repository.ClientRepository;
import com.neocamp.api_agendamento.repository.ProviderRepository;
import com.neocamp.api_agendamento.repository.ScheduleRepository;
import com.neocamp.api_agendamento.repository.WorkRepository;
import org.springframework.stereotype.Service;

@Service
public class ScheduleService {
    private final ClientRepository clientRepository;
    private final WorkRepository workRepository;
    private final ProviderRepository providerRepository;
    private final ScheduleRepository scheduleRepository;
    private final EmailService emailService;

    private final ViaCepService viaCepService;
    public ScheduleService(ViaCepService viaCepService, ScheduleRepository scheduleRepository, ProviderRepository providerRepository,
                           ClientRepository clientRepository, WorkRepository workRepository,
                           EmailService emailService) {
        this.viaCepService = viaCepService;
        this.scheduleRepository = scheduleRepository;
        this.providerRepository = providerRepository;
        this.clientRepository = clientRepository;
        this.workRepository = workRepository;
        this.emailService = emailService;
    }

    public ScheduleResponseDTO saveSchedule(ScheduleRequestDTO scheduleRequestDTO) {
        var address = viaCepService.getAddressByCep(scheduleRequestDTO.cep(), scheduleRequestDTO.number(), scheduleRequestDTO.complement());
        var schedule = new Schedule();
        schedule.setAddress(address);
        schedule.setDateTime(scheduleRequestDTO.date());
        schedule.setProvider(providerRepository.findById(scheduleRequestDTO.providerId())
                .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado!")));
        schedule.setClient(clientRepository.findById(scheduleRequestDTO.clientId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado!")));

        schedule.setWork(workRepository.findById(scheduleRequestDTO.workId())
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado!")));

        schedule.setStatus(Status.AGUARDANDO_CONFIRMACAO);
        var savedSchedule = scheduleRepository.save(schedule);
        emailService.sendScheduleNotification(savedSchedule);

        return new ScheduleResponseDTO(
                savedSchedule.getId(),
                savedSchedule.getClient().getName(),
                savedSchedule.getClient().getEmail(),
                savedSchedule.getClient().getPhone(),
                savedSchedule.getProvider().getName(),
                savedSchedule.getProvider().getEmail(),
                savedSchedule.getProvider().getPhone(),
                savedSchedule.getWork().getName(),
                savedSchedule.getWork().getSpecialty(),
                savedSchedule.getWork().getPrice(),
                savedSchedule.getDateTime(),
                savedSchedule.getStatus(),
                savedSchedule.getAddress()
        );
    }

    public String confirmSchedule(Long id) {
        var schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado!"));

        if (schedule.getStatus() != Status.AGUARDANDO_CONFIRMACAO) {
            throw new RuntimeException("Agendamento não está aguardando confirmação!");
        }

        schedule.setStatus(Status.CONFIRMADO);
        scheduleRepository.save(schedule);
        emailService.sendConfirmationToClient(schedule);

        return "Agendamento confirmado com sucesso!";
    }

    public String cancelSchedule(Long id) {
        var schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado!"));

        if (schedule.getStatus() == Status.CANCELADO) {
            throw new RuntimeException("Agendamento já está cancelado!");
        }

        schedule.setStatus(Status.CANCELADO);
        scheduleRepository.save(schedule);
        emailService.sendCancellationToClient(schedule);

        return "Agendamento cancelado com sucesso!";
    }
}
