package com.neocamp.api_agendamento.events;

import com.neocamp.api_agendamento.domain.entities.Schedule;
import org.springframework.context.ApplicationEvent;

public class RescheduleEvent{
    private final Schedule schedule;
    private final String initiatorType;
    private final Long initiatorId;
    
    public RescheduleEvent(Schedule schedule) {
        this.schedule = schedule;
        this.initiatorType = null;
        this.initiatorId = null;
    }
    
    public RescheduleEvent(Schedule schedule, String initiatorType, Long initiatorId) {
        this.schedule = schedule;
        this.initiatorType = initiatorType;
        this.initiatorId = initiatorId;
    }
    
    public Schedule getSchedule() {return schedule;}
    public String getInitiatorType() {return initiatorType;}
    public Long getInitiatorId() {return initiatorId;}
}
