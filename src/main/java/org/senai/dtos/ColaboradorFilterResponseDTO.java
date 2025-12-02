package org.senai.dtos;

import org.senai.model.Colaborador;
import java.util.List;
import java.util.stream.Collectors;

public record ColaboradorFilterResponseDTO(
    Long id,
    String nome,
    String matricula,
    String email,
    String cpf,
    String cargo,
    String departamento,
    List<TagDTO> tags
) {
    public static ColaboradorFilterResponseDTO fromEntity(Colaborador colaborador) {
        List<TagDTO> tags = colaborador.getTags().stream()
            .map(tag -> new TagDTO(tag.getId(), tag.getNome()))
            .collect(Collectors.toList());
        
        return new ColaboradorFilterResponseDTO(
            colaborador.getId(),
            colaborador.getNome(),
            colaborador.getMatricula(),
            colaborador.getEmail(),
            colaborador.getCpf(),
            colaborador.getCargo(),
            colaborador.getDepartamento(),
            tags
        );
    }

    public static ColaboradorFilterResponseDTO fromEntityWithFilteredTags(
            Colaborador colaborador, 
            List<String> relevantTagNames) {
        List<TagDTO> tags = colaborador.getTags().stream()
            .filter(tag -> relevantTagNames.contains(tag.getNome()))
            .map(tag -> new TagDTO(tag.getId(), tag.getNome()))
            .collect(Collectors.toList());
        
        return new ColaboradorFilterResponseDTO(
            colaborador.getId(),
            colaborador.getNome(),
            colaborador.getMatricula(),
            colaborador.getEmail(),
            colaborador.getCpf(),
            colaborador.getCargo(),
            colaborador.getDepartamento(),
            tags
        );
    }
}
