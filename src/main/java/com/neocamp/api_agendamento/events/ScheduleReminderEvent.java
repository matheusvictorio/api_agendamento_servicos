package com.neocamp.api_agendamento.events;

import com.neocamp.api_agendamento.domain.entities.Schedule;
import org.springframework.context.ApplicationEvent;

public class ScheduleReminderEvent extends ApplicationEvent {
    private final Schedule schedule;
    public ScheduleReminderEvent(Schedule schedule) {
        super(schedule);
        this.schedule = schedule;
    }
    public Schedule getSchedule() {
        return schedule;
    }
}

