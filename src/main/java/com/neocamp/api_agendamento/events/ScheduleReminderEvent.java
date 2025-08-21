package com.neocamp.api_agendamento.events;

import com.neocamp.api_agendamento.domain.entities.Schedule;
import org.springframework.context.ApplicationEvent;

public class ScheduleReminderEvent{
    private final Schedule schedule;
    public ScheduleReminderEvent(Schedule schedule) {
        this.schedule = schedule;
    }
    public Schedule getSchedule() {
        return schedule;
    }
}

