package org.senai.dtos;

import jakarta.validation.constraints.NotNull;

public record TrocarSupervisorDTO(
        @NotNull(message = "O ID do colaborador é obrigatório")
        Long colaboradorId,

        @NotNull(message = "O ID do novo supervisor é obrigatório")
        Long novoSupervisorId,

        @NotNull(message = "O tipo de supervisor é obrigatório")
        Long tipoSupervisorId,

        String motivo
) {
}
