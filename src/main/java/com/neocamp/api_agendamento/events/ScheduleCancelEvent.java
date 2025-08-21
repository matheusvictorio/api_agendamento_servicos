package com.neocamp.api_agendamento.events;

import com.neocamp.api_agendamento.domain.entities.Schedule;

public class ScheduleCancelEvent {
    private final Schedule schedule;

    public ScheduleCancelEvent(Schedule schedule) {
        this.schedule = schedule;
    }

    public Schedule getSchedule() {return schedule;}
}
