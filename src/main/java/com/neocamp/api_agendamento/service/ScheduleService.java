package com.neocamp.api_agendamento.service;

import com.neocamp.api_agendamento.domain.dto.request.ScheduleRequestDTO;
import com.neocamp.api_agendamento.domain.dto.response.ScheduleResponseDTO;
import com.neocamp.api_agendamento.domain.entities.Schedule;
import com.neocamp.api_agendamento.domain.enums.Status;
import com.neocamp.api_agendamento.events.*;
import com.neocamp.api_agendamento.repository.ClientRepository;
import com.neocamp.api_agendamento.repository.ProviderRepository;
import com.neocamp.api_agendamento.repository.ScheduleRepository;
import com.neocamp.api_agendamento.repository.WorkRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ScheduleService {
    private final ClientRepository clientRepository;
    private final WorkRepository workRepository;
    private final ProviderRepository providerRepository;
    private final ScheduleRepository scheduleRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    private final ViaCepService viaCepService;
    public ScheduleService(ViaCepService viaCepService, ScheduleRepository scheduleRepository, ProviderRepository providerRepository,
                           ClientRepository clientRepository, WorkRepository workRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.viaCepService = viaCepService;
        this.scheduleRepository = scheduleRepository;
        this.providerRepository = providerRepository;
        this.clientRepository = clientRepository;
        this.workRepository = workRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public ScheduleResponseDTO saveSchedule(ScheduleRequestDTO scheduleRequestDTO) {
        var address = viaCepService.getAddressByCep(scheduleRequestDTO.cep(), scheduleRequestDTO.number(), scheduleRequestDTO.complement());
        var scheduleDateTime = scheduleRequestDTO.date();
        var provider = providerRepository.findById(scheduleRequestDTO.providerId())
                .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado!"));
        var client = clientRepository.findById(scheduleRequestDTO.clientId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado!"));
        var work = workRepository.findById(scheduleRequestDTO.workId())
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado!"));
        if (!work.isActive()) {
            throw new RuntimeException("Este serviço está temporariamente indisponível para agendamento!");
        }

        if (scheduleRepository.existsByClientIdAndDateTime(client.getId(), scheduleDateTime)) {
            throw new RuntimeException("O cliente já possui um agendamento neste horário!");
        }
        if (scheduleRepository.existsByProviderIdAndDateTime(provider.getId(), scheduleDateTime)) {
            throw new RuntimeException("O prestador já possui um agendamento neste horário!");
        }

        LocalDate day = scheduleDateTime.toLocalDate();
        LocalDateTime startOfDay = day.atStartOfDay();
        LocalDateTime endOfDay = day.atTime(LocalTime.MAX);
        long clientDailyCount = scheduleRepository.countByClientIdAndDateTimeBetweenAndStatusNot(client.getId(), startOfDay, endOfDay, Status.CANCELADO);
        long providerDailyCount = scheduleRepository.countByProviderIdAndDateTimeBetweenAndStatusNot(provider.getId(), startOfDay, endOfDay, Status.CANCELADO);
        if (clientDailyCount >= 1) {
            throw new RuntimeException("O cliente só pode agendar 1 serviço por dia!");
        }
        if (providerDailyCount >= 3) {
            throw new RuntimeException("O prestador só pode atender 3 serviços por dia!");
        }

        long clientActiveCount = scheduleRepository.countByClientIdAndStatusNot(client.getId(), Status.CANCELADO);
        long providerActiveCount = scheduleRepository.countByProviderIdAndStatusNot(provider.getId(), Status.CANCELADO);
        if (clientActiveCount >= 10) {
            throw new RuntimeException("O cliente atingiu o limite de agendamentos ativos!");
        }
        if (providerActiveCount >= 30) {
            throw new RuntimeException("O prestador atingiu o limite de agendamentos ativos!");
        }

        var schedule = new Schedule();
        schedule.setAddress(address);
        schedule.setDateTime(scheduleDateTime);
        schedule.setProvider(provider);
        schedule.setClient(client);
        schedule.setWork(work);
        schedule.setStatus(Status.AGUARDANDO_CONFIRMACAO);
        var savedSchedule = scheduleRepository.save(schedule);
        applicationEventPublisher.publishEvent(new ScheduleCreateEvent(savedSchedule));

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
        applicationEventPublisher.publishEvent(new ScheduleConfirmEvent(schedule));

        return "Agendamento confirmado com sucesso!";
    }

    public String cancelSchedule(Long id) {
        var schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado!"));


        LocalDateTime now = LocalDateTime.now();
        if (schedule.getDateTime().isBefore(now.plusHours(24))) {
            throw new RuntimeException("Cancelamento só permitido com pelo menos 24h de antecedência!");
        }

        if (schedule.getStatus() == Status.CANCELADO) {
            throw new RuntimeException("Agendamento já está cancelado!");
        }

        schedule.setStatus(Status.CANCELADO);
        scheduleRepository.save(schedule);
        applicationEventPublisher.publishEvent(new ScheduleCancelEvent(schedule));

        return "Agendamento cancelado com sucesso!";
    }

    public void submitRating(Long scheduleId, String type, Double rating) {
        var schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado!"));
        if (schedule.getStatus() != Status.FINALIZADO) {
            throw new RuntimeException("A avaliação só pode ser feita após o agendamento ser finalizado.");
        }

        switch (type.toLowerCase()) {
            case "provider":
                var provider = schedule.getProvider();
                provider.setAverageRating(
                        updateAverage(provider.getAverageRating(), provider.getRatingCount(), rating)
                );
                provider.setRatingCount(
                        (provider.getRatingCount() == null ? 0 : provider.getRatingCount()) + 1
                );
                providerRepository.save(provider);
                break;
            case "client":
                var client = schedule.getClient();
                client.setAverageRating(
                        updateAverage(client.getAverageRating(), client.getRatingCount(), rating)
                );
                client.setRatingCount(
                        (client.getRatingCount() == null ? 0 : client.getRatingCount()) + 1
                );
                clientRepository.save(client);
                break;
            case "work":
                var work = schedule.getWork();
                work.setAverageRating(
                        updateAverage(work.getAverageRating(), work.getRatingCount(), rating)
                );
                work.setRatingCount(
                        (work.getRatingCount() == null ? 0 : work.getRatingCount()) + 1
                );
                workRepository.save(work);
                break;
            default:
                throw new RuntimeException("Tipo de avaliação inválido!");
        }
    }

    private Double updateAverage(Double currentAverage, Integer currentCount, Double newRating) {
        if (currentCount == null) currentCount = 0;
        if (currentAverage == null) currentAverage = 0.0;
        return ((currentAverage * currentCount) + newRating) / (currentCount + 1);
    }

    public String finalizeSchedule(Long id) {
        var schedule = scheduleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Agendamento não encontrado!"));
        if (schedule.getStatus() != Status.CONFIRMADO) {
            throw new RuntimeException("Só é possível finalizar agendamentos confirmados!");
        }
        schedule.setStatus(Status.FINALIZADO);
        scheduleRepository.save(schedule);
        applicationEventPublisher.publishEvent(new ScheduleFollowUpEvent(schedule));
        return "Agendamento finalizado com sucesso!";
    }

    @Scheduled(fixedRate = 3600000)
    public void sendReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in24h = now.plusHours(24);
        LocalDateTime in1h = now.plusHours(1);

        List<Schedule> schedules24h = scheduleRepository.findByDateTimeBetweenAndStatus(
            in24h.minusMinutes(30), in24h.plusMinutes(30), Status.CONFIRMADO);
        for (Schedule schedule : schedules24h) {
            applicationEventPublisher.publishEvent(new ScheduleReminderEvent(schedule));
        }

        List<Schedule> schedules1h = scheduleRepository.findByDateTimeBetweenAndStatus(
            in1h.minusMinutes(30), in1h.plusMinutes(30), Status.CONFIRMADO);
        for (Schedule schedule : schedules1h) {
            applicationEventPublisher.publishEvent(new ScheduleReminderEvent(schedule));
        }
    }
}
