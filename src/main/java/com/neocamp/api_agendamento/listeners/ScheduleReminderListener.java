package com.neocamp.api_agendamento.listeners;

import com.neocamp.api_agendamento.events.ScheduleReminderEvent;
import com.neocamp.api_agendamento.service.EmailService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ScheduleReminderListener {
    private final EmailService emailService;
    public ScheduleReminderListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @EventListener
    @Async
    public void handleReminder(ScheduleReminderEvent event) {
        emailService.sendReminderToClient(event.getSchedule());
        emailService.sendReminderToProvider(event.getSchedule());
    }
}

