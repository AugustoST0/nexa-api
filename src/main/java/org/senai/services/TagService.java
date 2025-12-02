package org.senai.services;

import org.senai.dtos.TagCreateDTO;
import org.senai.dtos.TagUpdateDTO;
import org.senai.exception.exceptions.RegisterNotFoundException;
import org.senai.model.Grupo;
import org.senai.model.Tag;
import org.senai.repositories.TagRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class TagService {

    @Inject
    TagRepository tagRepository;

    @Inject
    GrupoService grupoService;

    public List<Tag> getAll() {
        return tagRepository.listAll();
    }

    public Tag getById(Long id) {
        Tag tag = tagRepository.findById(id);

        if (tag == null) {
            throw new RegisterNotFoundException("Tag não encontrada");
        }

        return tag;
    }

    public List<Tag> findAllById(List<Long> ids) {
        return tagRepository.findAllById(ids);
    }

    public List<Tag> getTagsByGrupoId(Long id) {
        Grupo grupo = grupoService.getById(id);
        return grupo.getTags();
    }

    public List<Tag> getTagsNotInGrupo(Long grupoId) {
        Grupo grupo = grupoService.getById(grupoId);
        return tagRepository.findTagsNotInGrupo(grupoId);
    }

    @Transactional
    public Tag create(TagCreateDTO dto) {
        Tag tag = new Tag();
        tag.setNome(dto.nome());
        tag.setDescricao(dto.descricao());

        return tagRepository.save(tag);
    }

    @Transactional
    public Tag update(Long id, TagUpdateDTO dto) {
        Tag tag = getById(id);

        if (dto.nome() != null) {
            tag.setNome(dto.nome());
        }

        if (dto.descricao() != null) {
            tag.setDescricao(dto.descricao());
        }

        return tag;
    }

    @Transactional
    public void linkToGrupo(Long tagId, Long grupoId) {
        Tag tag = getById(tagId);
        Grupo grupo = grupoService.getById(grupoId);

        if (!tag.getGrupos().contains(grupo)) {
            tag.getGrupos().add(grupo);
            grupo.getTags().add(tag);
        }
    }

    @Transactional
    public void unlinkFromGrupo(Long tagId, Long grupoId) {
        Tag tag = getById(tagId);
        Grupo grupo = grupoService.getById(grupoId);

        tag.getGrupos().remove(grupo);
        grupo.getTags().remove(tag);
    }

    @Transactional
    public void delete(Long id) {
        Tag tag = getById(id);

        for (Grupo grupo : tag.getGrupos()) {
            grupo.getTags().remove(tag);
        }

        tag.getGrupos().clear();

        tagRepository.delete(tag);
    }
}