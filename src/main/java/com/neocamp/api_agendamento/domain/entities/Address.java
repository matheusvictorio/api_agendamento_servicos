package com.neocamp.api_agendamento.domain.entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Embeddable
@Getter
@Setter
@Service
@AllArgsConstructor
@NoArgsConstructor
public class Address {
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String uf;
    private String cep;

    @Override
    public String toString() {
        return String.format("%s, %s - %s, %s - CEP: %s",
                this.logradouro,
                this.numero,
                this.bairro,
                this.cidade,
                this.cep
        );
    }

}
