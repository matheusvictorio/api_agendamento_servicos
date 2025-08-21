package com.neocamp.api_agendamento.service;

import com.neocamp.api_agendamento.domain.dto.request.RescheduleUpdateDTO;
import com.neocamp.api_agendamento.domain.dto.request.ScheduleRequestDTO;
import com.neocamp.api_agendamento.domain.dto.response.ScheduleResponseDTO;
import com.neocamp.api_agendamento.domain.entities.Address;
import com.neocamp.api_agendamento.domain.entities.Schedule;
import com.neocamp.api_agendamento.domain.enums.Status;
import com.neocamp.api_agendamento.events.*;
import com.neocamp.api_agendamento.repository.ClientRepository;
import com.neocamp.api_agendamento.repository.ProviderRepository;
import com.neocamp.api_agendamento.repository.ScheduleRepository;
import com.neocamp.api_agendamento.repository.WorkRepository;
import com.neocamp.api_agendamento.config.security.JwtContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScheduleService {
    private final ClientRepository clientRepository;
    private final WorkRepository workRepository;
    private final ProviderRepository providerRepository;
    private final ScheduleRepository scheduleRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final JwtContext jwtContext;

    private final ViaCepService viaCepService;
    public ScheduleService(ViaCepService viaCepService, ScheduleRepository scheduleRepository, ProviderRepository providerRepository,
                           ClientRepository clientRepository, WorkRepository workRepository, ApplicationEventPublisher applicationEventPublisher,
                           JwtContext jwtContext) {
        this.viaCepService = viaCepService;
        this.scheduleRepository = scheduleRepository;
        this.providerRepository = providerRepository;
        this.clientRepository = clientRepository;
        this.workRepository = workRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.jwtContext = jwtContext;
    }

    public ScheduleResponseDTO saveSchedule(ScheduleRequestDTO scheduleRequestDTO) {
        // Get client from JWT token
        String currentUserType = jwtContext.getCurrentUserType();
        if (!"CLIENT".equals(currentUserType)) {
            throw new RuntimeException("Apenas clientes podem criar agendamentos!");
        }
        
        Long clientId = jwtContext.getCurrentUserId();
        var address = viaCepService.getAddressByCep(scheduleRequestDTO.cep(), scheduleRequestDTO.number(), scheduleRequestDTO.complement());
        var scheduleDateTime = scheduleRequestDTO.date();
        var provider = providerRepository.findById(scheduleRequestDTO.providerId())
                .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado!"));
        var client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado!"));
        var work = workRepository.findById(scheduleRequestDTO.workId())
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado!"));
        if (!work.isActive()) {
            throw new RuntimeException("Este serviço está temporariamente indisponível para agendamento!");
        }

        if (scheduleRepository.existsByClientIdAndDateTime(clientId, scheduleDateTime)) {
            throw new RuntimeException("O cliente já possui um agendamento neste horário!");
        }
        if (scheduleRepository.existsByProviderIdAndDateTime(provider.getId(), scheduleDateTime)) {
            throw new RuntimeException("O prestador já possui um agendamento neste horário!");
        }

        LocalDate day = scheduleDateTime.toLocalDate();
        LocalDateTime startOfDay = day.atStartOfDay();
        LocalDateTime endOfDay = day.atTime(LocalTime.MAX);
        long clientDailyCount = scheduleRepository.countByClientIdAndDateTimeBetweenAndStatusNot(clientId, startOfDay, endOfDay, Status.CANCELADO);
        long providerDailyCount = scheduleRepository.countByProviderIdAndDateTimeBetweenAndStatusNot(provider.getId(), startOfDay, endOfDay, Status.CANCELADO);
        if (clientDailyCount >= 1) {
            throw new RuntimeException("O cliente só pode agendar 1 serviço por dia!");
        }
        if (providerDailyCount >= 3) {
            throw new RuntimeException("O prestador só pode atender 3 serviços por dia!");
        }

        long clientActiveCount = scheduleRepository.countByClientIdAndStatusNot(clientId, Status.CANCELADO);
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
        schedule.setConfirmedAt(LocalDateTime.now());
        scheduleRepository.save(schedule);
        applicationEventPublisher.publishEvent(new ScheduleConfirmEvent(schedule));

        return "Agendamento confirmado com sucesso!";
    }

    public void cancelSchedule(Long id) {
        var schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado!"));


        LocalDateTime now = LocalDateTime.now();
        if (schedule.getDateTime().isBefore(now.plusHours(24))) {
            throw new RuntimeException("Cancelamento só permitido com pelo menos 24h de antecedência!");
        }
        if (schedule.getStatus() == Status.CONFIRMADO) {
            if (schedule.getConfirmedAt() != null && schedule.getConfirmedAt().isAfter(now.minusHours(1))) {
                throw new RuntimeException("Cancelamento só permitido com pelo menos 1h da confirmação!");
            }
        }

        if (schedule.getStatus() == Status.CANCELADO) {
            throw new RuntimeException("Agendamento já está cancelado!");
        }

        schedule.setStatus(Status.CANCELADO);
        scheduleRepository.save(schedule);
        applicationEventPublisher.publishEvent(new ScheduleCancelEvent(schedule));
    }

    public void submitRating(Long scheduleId, String type, Double rating) {
        var schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new RuntimeException("Agendamento não encontrado!"));
        if (schedule.getStatus() != Status.FINALIZADO) {
            throw new RuntimeException("A avaliação só pode ser feita após o agendamento ser finalizado.");
        }
        if (Boolean.TRUE.equals(schedule.getUserRated())) {
            throw new RuntimeException("O usuário já avaliou este agendamento.");
        }
        var provider = schedule.getProvider();
        var work = schedule.getWork();
        var client = schedule.getClient();
        switch ( (type.toLowerCase())) {
            case "provider":
                provider.setAverageRating(updateAverage(provider.getAverageRating(), provider.getRatingCount(), rating));
                provider.setRatingCount(provider.getRatingCount() + 1);
                break;
            case "work":
                work.setAverageRating(updateAverage(work.getAverageRating(), work.getRatingCount(), rating));
                work.setRatingCount(work.getRatingCount() + 1);
                break;
            case "client":
                client.setAverageRating(updateAverage(client.getAverageRating(), client.getRatingCount(), rating));
                client.setRatingCount(client.getRatingCount() + 1);
                break;
            default:
                throw new RuntimeException("Tipo de avaliação inválido.");
        }
        schedule.setUserRated(true);
        scheduleRepository.save(schedule);
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

    public ScheduleResponseDTO rescheduleSchedule(Long id, RescheduleUpdateDTO rescheduleUpdateDTO) {
        var schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado!"));
        if (schedule.getStatus() == Status.CANCELADO || schedule.getStatus() == Status.FINALIZADO) {
            throw new RuntimeException("Não é possível remarcar um agendamento cancelado ou finalizado.");
        }
        
        // Check if the current user has permission to reschedule
        String currentUserType = jwtContext.getCurrentUserType();
        Long currentUserId = jwtContext.getCurrentUserId();
        
        boolean canReschedule = false;
        if ("CLIENT".equals(currentUserType) && schedule.getClient().getId().equals(currentUserId)) {
            canReschedule = true;
        } else if ("PROVIDER".equals(currentUserType) && schedule.getProvider().getId().equals(currentUserId)) {
            canReschedule = true;
        }
        
        if (!canReschedule) {
            throw new RuntimeException("Você não tem permissão para remarcar este agendamento!");
        }

        if (scheduleRepository.existsByClientIdAndDateTime(schedule.getClient().getId(), rescheduleUpdateDTO.date())) {
            throw new RuntimeException("O cliente já possui um agendamento neste novo horário!");
        }
        if (scheduleRepository.existsByProviderIdAndDateTime(schedule.getProvider().getId(), rescheduleUpdateDTO.date())) {
            throw new RuntimeException("O prestador já possui um agendamento neste novo horário!");
        }

        if (rescheduleUpdateDTO.cep() != null) {
            if (rescheduleUpdateDTO.number() == null) {
                throw new RuntimeException("Para atualizar o CEP, o número do endereço também deve ser informado.");
            }
            if (rescheduleUpdateDTO.cep().equalsIgnoreCase(schedule.getAddress().getCep())) {
                throw new RuntimeException("O CEP informado é o mesmo do agendamento original!");
            }
            var address = viaCepService.getAddressByCep(rescheduleUpdateDTO.cep(), rescheduleUpdateDTO.number(), rescheduleUpdateDTO.complement());
            schedule.setAddress(address);
        } else if (rescheduleUpdateDTO.number() != null) {
            Address currentAddress = schedule.getAddress();
            currentAddress.setNumero(rescheduleUpdateDTO.number());
            if (rescheduleUpdateDTO.complement() != null) {
                currentAddress.setComplemento(rescheduleUpdateDTO.complement());
            }
            schedule.setAddress(currentAddress);
        }
        if (rescheduleUpdateDTO.date() != null) {
            schedule.setDateTime(rescheduleUpdateDTO.date());
        }
        schedule.setStatus(Status.AGUARDANDO_CONFIRMACAO);
        scheduleRepository.save(schedule);
        
        // Create reschedule event with information about who initiated the reschedule
        RescheduleEvent rescheduleEvent = new RescheduleEvent(schedule, currentUserType, currentUserId);
        applicationEventPublisher.publishEvent(rescheduleEvent);

        return new ScheduleResponseDTO(
                schedule.getId(),
                schedule.getClient().getName(),
                schedule.getClient().getEmail(),
                schedule.getClient().getPhone(),
                schedule.getProvider().getName(),
                schedule.getProvider().getEmail(),
                schedule.getProvider().getPhone(),
                schedule.getWork().getName(),
                schedule.getWork().getSpecialty(),
                schedule.getWork().getPrice(),
                schedule.getDateTime(),
                schedule.getStatus(),
                schedule.getAddress()
        );
    }

    //adicionar para poder ver apenas os agendamentos ativos que ainda não foram finalizados
    //posso adicionar filtros aqui -> Status, Data, provider, work, specialty -> posso trocar para query para aumentar a performance e evitar a conversão de todos os registros para DTO
    // adicionar paginação
    public List<ScheduleResponseDTO> getSchedulesByClient() {
        String currentUserType = jwtContext.getCurrentUserType();
        if (!"CLIENT".equals(currentUserType)) {
            throw new RuntimeException("Apenas clientes podem visualizar seus próprios agendamentos!");
        }
        
        Long clientId = jwtContext.getCurrentUserId();
        List<Schedule> schedules = scheduleRepository.findByClientId(clientId);
        return schedules.stream()
                .map(schedule -> new ScheduleResponseDTO(
                        schedule.getId(),
                        schedule.getClient().getName(),
                        schedule.getClient().getEmail(),
                        schedule.getClient().getPhone(),
                        schedule.getProvider().getName(),
                        schedule.getProvider().getEmail(),
                        schedule.getProvider().getPhone(),
                        schedule.getWork().getName(),
                        schedule.getWork().getSpecialty(),
                        schedule.getWork().getPrice(),
                        schedule.getDateTime(),
                        schedule.getStatus(),
                        schedule.getAddress()
                ))
                .toList();
    }

    // adicionar paginação
    public List<ScheduleResponseDTO> getSchedulesByProvider() {
        String currentUserType = jwtContext.getCurrentUserType();
        if (!"PROVIDER".equals(currentUserType)) {
            throw new RuntimeException("Apenas prestadores podem visualizar seus próprios agendamentos!");
        }
        
        Long providerId = jwtContext.getCurrentUserId();
        List<Schedule> schedules = scheduleRepository.findByProviderId(providerId);
        return schedules.stream()
                .map(schedule -> new ScheduleResponseDTO(
                        schedule.getId(),
                        schedule.getClient().getName(),
                        schedule.getClient().getEmail(),
                        schedule.getClient().getPhone(),
                        schedule.getProvider().getName(),
                        schedule.getProvider().getEmail(),
                        schedule.getProvider().getPhone(),
                        schedule.getWork().getName(),
                        schedule.getWork().getSpecialty(),
                        schedule.getWork().getPrice(),
                        schedule.getDateTime(),
                        schedule.getStatus(),
                        schedule.getAddress()
                ))
                .toList();
    }


    // adicionar paginação - adicionar o usuário poder decidir a data inicial e final para buscar os horários disponíveis
    // adicionar o usuário poder decidir a duração do serviço para buscar os horários disponíveis
    // adicionar o usuário poder entrar com o dia que ele quer buscar os horários disponíveis
    // adicionar o usuário poder entrar com o horário que ele quer buscar os horários
    // adicionar comparação via matriz(comparação de queries) -> performance
    public List<LocalDateTime> getProviderAvailableSlots(Long providerId) {
        providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado!"));

        List<LocalDateTime> availableSlots = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int d = 0; d < 7; d++) {
            LocalDate date = today.plusDays(d);
            for (int h = 8; h < 19; h++) {
                LocalDateTime slot = date.atTime(h, 0);
                boolean ocupado = scheduleRepository.existsByProviderIdAndDateTime(providerId, slot);
                if (!ocupado) {
                    availableSlots.add(slot);
                }
            }
        }
        return availableSlots;
    }
}
