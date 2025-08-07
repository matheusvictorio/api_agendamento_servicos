package com.neocamp.api_agendamento.domain.dto.request;

import com.neocamp.api_agendamento.domain.enums.Specialty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProviderRequestDTO(
        @NotBlank
        String name,
        @NotBlank
        @Email(message = "Email deve ser válido")
        String email,
        @NotBlank
        @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.])[A-Za-z\\d@$!%*?&.]{8,}$",
                message = "A senha deve conter pelo menos uma letra maiúscula, uma minúscula, um número e um caractere especial."
        )
        String password,
        @NotBlank
        @Pattern(
                regexp = "^(\\(\\d{2}\\)\\s?\\d{4,5}-\\d{4}|\\d{10,11})$",
                message = "Telefone deve estar em formato válido, como (11) 91234-5678 ou 11912345678"
        )
        String phone,
        @NotBlank
        @Pattern(
                regexp = "^\\d{5}-?\\d{3}$",
                message = "CEP deve estar em formato válido, como 12345-678 ou 12345678"
        )
        String cep,
        @NotBlank
        String number,
        String complement,
        @NotBlank
        Specialty specialty
) {
}
