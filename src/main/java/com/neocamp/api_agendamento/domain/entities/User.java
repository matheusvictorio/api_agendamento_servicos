package com.neocamp.api_agendamento.domain.entities;

import org.springframework.security.core.userdetails.UserDetails;

public interface User extends UserDetails {
    Long getId();
    String getName();
    String getEmail();
    String getPhone();
    Address getAddress();
    String getUserType();
}
