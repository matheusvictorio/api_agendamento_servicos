package com.neocamp.api_agendamento.listeners;

import com.neocamp.api_agendamento.events.ScheduleFollowUpEvent;
import com.neocamp.api_agendamento.service.EmailService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ScheduleFollowUpListener {
    private final EmailService emailService;
    public ScheduleFollowUpListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @EventListener
    @Async
    public void handleFollowUp(ScheduleFollowUpEvent event) {
        emailService.sendFollowUpToClientWithRating(event.getSchedule());
        emailService.sendFollowUpToProviderWithRating(event.getSchedule());
    }
}
