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
public class Provider implements User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true)
    private String email;
    private String password;
    private String phone;
    private Address address;
    @Enumerated(EnumType.STRING)
    private Specialty specialty;
    private Boolean active = true;

    public Provider(String name, String email, String password, String phone, Address address, Specialty specialty) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.specialty = specialty;
    }
}
