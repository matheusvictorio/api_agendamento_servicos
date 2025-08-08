package com.neocamp.api_agendamento.domain.entities;

import com.neocamp.api_agendamento.domain.enums.Specialty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Work {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(length = 500)
    private String description;
    private Double price;
    @Enumerated(EnumType.STRING)
    private Specialty specialty;
    @ManyToOne
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;
}
