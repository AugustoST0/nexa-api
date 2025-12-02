package org.senai.dtos;

import org.senai.model.Colaborador;

import java.time.LocalDate;
import java.util.List;

public record ColaboradorResponseDTO(
        Long id,
        String nome,
        String matricula,
        String email,
        String cpf,
        LocalDate dataNascimento,
        LocalDate dataAdmissao,
        String cargo,
        String departamento,
        List<TagDTO> tags
) {
    public static ColaboradorResponseDTO fromEntity(Colaborador colaborador) {
        return new ColaboradorResponseDTO(
                colaborador.getId(),
                colaborador.getNome(),
                colaborador.getMatricula(),
                colaborador.getEmail(),
                colaborador.getCpf(),
                colaborador.getDataNascimento(),
                colaborador.getDataAdmissao(),
                colaborador.getCargo(),
                colaborador.getDepartamento(),
                colaborador.getTags() != null ?
                        colaborador.getTags().stream()
                                .map(tag -> new TagDTO(tag.getId(), tag.getNome()))
                                .toList() :
                        List.of()
        );
    }
}