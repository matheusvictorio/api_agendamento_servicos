package com.neocamp.api_agendamento.listeners;

import com.neocamp.api_agendamento.events.ScheduleCancelEvent;
import com.neocamp.api_agendamento.service.EmailService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ScheduleCancelEmailListener {
    private final EmailService emailService;

    public ScheduleCancelEmailListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @EventListener
    @Async
    public void handleScheduleCancelEvent(ScheduleCancelEvent scheduleCancelEvent) {
        emailService.sendCancellationToClient(scheduleCancelEvent.getSchedule());
    }
}
