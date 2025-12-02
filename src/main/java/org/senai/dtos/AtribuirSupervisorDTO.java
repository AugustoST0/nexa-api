package org.senai.dtos;

import jakarta.validation.constraints.NotNull;

public record AtribuirSupervisorDTO(
        @NotNull(message = "O ID do supervisor é obrigatório")
        Long supervisorId,

        @NotNull(message = "O tipo de supervisor é obrigatório")
        Long tipoSupervisorId,

        String observacoes
) {
}
