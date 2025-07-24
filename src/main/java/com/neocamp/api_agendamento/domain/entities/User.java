package com.neocamp.api_agendamento.domain.entities;

public interface User {
    Long getId();
    String getName();
    String getEmail();
    String getPassword();
    String getPhone();
    Address getAddress();

}
