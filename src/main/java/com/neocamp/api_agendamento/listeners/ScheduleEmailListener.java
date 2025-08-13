package com.neocamp.api_agendamento.listeners;

import com.neocamp.api_agendamento.events.ScheduleCreateEvent;
import com.neocamp.api_agendamento.service.EmailService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ScheduleEmailListener {
    private final EmailService emailService;

    public ScheduleEmailListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @EventListener
    @Async
    public void handleScheduleCreateEvent(ScheduleCreateEvent scheduleCreateEvent) {
        emailService.sendScheduleNotification(scheduleCreateEvent.getSchedule());
    }
}
