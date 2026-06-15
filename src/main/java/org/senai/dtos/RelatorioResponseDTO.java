package org.senai.dtos;

import org.senai.model.Relatorio;

import java.time.LocalDateTime;

public record RelatorioResponseDTO(
        Long id,
        String titulo,
        String parametros,
        LocalDateTime geradoEm,
        Long grupoId,
        int totalColaboradores
) {
    public static RelatorioResponseDTO fromEntity(Relatorio relatorio) {
        return new RelatorioResponseDTO(
                relatorio.getId(),
                relatorio.getTitulo(),
                relatorio.getParametros(),
                relatorio.getGeradoEm(),
                relatorio.getGrupo() != null ? relatorio.getGrupo().getId() : null,
                relatorio.getColaboradores() != null ? relatorio.getColaboradores().size() : 0
        );
    }
}
