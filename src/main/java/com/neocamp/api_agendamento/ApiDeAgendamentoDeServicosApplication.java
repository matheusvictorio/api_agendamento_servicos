package com.neocamp.api_agendamento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ApiDeAgendamentoDeServicosApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiDeAgendamentoDeServicosApplication.class, args);
    }

}
