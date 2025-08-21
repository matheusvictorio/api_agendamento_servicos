package com.neocamp.api_agendamento.domain.dto.request;

import com.neocamp.api_agendamento.domain.enums.Specialty;
import jakarta.validation.constraints.*;

public record RegisterRequestDTO(
        @NotBlank(message = "Nome é obrigatório")
        String name,
        
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email deve ter um formato válido")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.])[A-Za-z\\d@$!%*?&.]{8,}$",
                message = "A senha deve conter pelo menos uma letra maiúscula, uma minúscula, um número e um caractere especial."
        )
        String password,
        
        @NotBlank(message = "Telefone é obrigatório")
        String phone,
        
        @NotBlank(message = "Tipo de usuário é obrigatório")
        @Pattern(regexp = "CLIENT|PROVIDER", message = "Tipo deve ser CLIENT ou PROVIDER")
        String userType,

        @NotBlank(message = "CEP é obrigatório")
        @Pattern(
                regexp = "^\\d{5}-?\\d{3}$",
                message = "CEP deve estar em formato válido, como 12345-678 ou 12345678"
        )
        String cep,
        
        @NotBlank(message = "Número é obrigatório")
        String number,
        
        String complement,

        Specialty specialty
) {
}
