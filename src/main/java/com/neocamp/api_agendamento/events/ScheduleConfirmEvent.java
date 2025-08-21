package com.neocamp.api_agendamento.events;

import com.neocamp.api_agendamento.domain.entities.Schedule;

public class ScheduleConfirmEvent {
    private final Schedule schedule;

    public ScheduleConfirmEvent(Schedule schedule) {
        this.schedule = schedule;
    }

    public Schedule getSchedule() {return schedule;}
}
