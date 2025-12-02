package org.senai.dtos;

import jakarta.validation.constraints.Size;

public record TagUpdateDTO(
        @Size(max = 100, message = "O nome não pode ter mais de 100 caracteres")
        String nome,

        @Size(max = 300, message = "A descrição não pode ter mais de 300 caracteres")
        String descricao,

        Long grupoId
) {
}
