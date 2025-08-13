package com.neocamp.api_agendamento.listeners;

import com.neocamp.api_agendamento.events.ScheduleConfirmEvent;
import com.neocamp.api_agendamento.service.EmailService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ScheduleConfirmEmailListener {
    private final EmailService emailService;

    public ScheduleConfirmEmailListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @EventListener
    @Async
    public void handleScheduleConfirmEvent(ScheduleConfirmEvent scheduleConfirmEvent) {
        emailService.sendConfirmationToClient(scheduleConfirmEvent.getSchedule());
    }
}
