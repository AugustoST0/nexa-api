package org.senai.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MigrarSupervisionadosDTO(
        @NotNull(message = "O ID do supervisor antigo é obrigatório")
        Long supervisorAntigoId,

        @NotNull(message = "O ID do supervisor novo é obrigatório")
        Long supervisorNovoId,

        @Size(max = 500, message = "O motivo não pode ter mais de 500 caracteres")
        String motivo,

        List<Long> colaboradorIds,

        Long tipoSupervisorId
) {
}
