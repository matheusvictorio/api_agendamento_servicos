package com.neocamp.api_agendamento.listeners;

import com.neocamp.api_agendamento.events.RescheduleEvent;
import com.neocamp.api_agendamento.service.EmailService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class RescheduleListener {
    private EmailService emailService;

    public RescheduleListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @EventListener
    @Async
    public void handleRescheduleEvent(RescheduleEvent rescheduleEvent) {
        String initiatorType = rescheduleEvent.getInitiatorType();

        if ("CLIENT".equals(initiatorType)) {
            emailService.sendRescheduleNotificationToProvider(rescheduleEvent.getSchedule());
        } else if ("PROVIDER".equals(initiatorType)) {
            emailService.sendRescheduleNotificationToClient(rescheduleEvent.getSchedule());
        } else {
            emailService.sendRescheduleNotificationToProvider(rescheduleEvent.getSchedule());
            emailService.sendRescheduleNotificationToClient(rescheduleEvent.getSchedule());
        }
    }
}