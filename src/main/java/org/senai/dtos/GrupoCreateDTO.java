package org.senai.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record GrupoCreateDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 1500, message = "O nome não pode ter mais de 100 caracteres")
        String nome,

        List<String> tokens,

        List<Long> supervisorIds,

        LocalDate dataAdmissaoInicio,

        LocalDate dataAdmissaoFim
) {}