package org.senai.dtos;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record ColaboradorCreateUpdateDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome não pode ter mais de 100 caracteres")
        String nome,

        @NotBlank(message = "A matrícula é obrigatória")
        @Size(max = 20, message = "A matrícula não pode ter mais de 20 caracteres")
        String matricula,

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "Email inválido")
        @Size(max = 150, message = "O email não pode ter mais de 150 caracteres")
        String email,

        @NotBlank(message = "O CPF é obrigatório")
        @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{11}", message = "CPF inválido")
        String cpf,

        @NotNull(message = "A data de nascimento é obrigatória")
        @Past(message = "A data de nascimento deve ser no passado")
        LocalDate dataNascimento,

        @NotNull(message = "A data de admissão é obrigatória")
        @PastOrPresent(message = "A data de admissão não pode ser futura")
        LocalDate dataAdmissao,

        @NotBlank(message = "O cargo é obrigatório")
        String cargo,

        @NotBlank(message = "O departamento é obrigatório")
        String departamento,

        List<Long> tagIds
) {
}