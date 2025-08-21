package com.neocamp.api_agendamento.domain.entities;

import com.neocamp.api_agendamento.domain.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateTime;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Address address;

    @ManyToOne
    @JoinColumn(name = "work_id", nullable = false)
    private Work work;

    @ManyToOne
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    private Boolean userRated = false;
    private LocalDateTime confirmedAt;
}
