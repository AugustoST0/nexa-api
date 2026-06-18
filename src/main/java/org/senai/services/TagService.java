package org.senai.services;

import org.senai.dtos.TagCreateDTO;
import org.senai.dtos.TagUpdateDTO;
import org.senai.exception.exceptions.RegisterNotFoundException;
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
    public void delete(Long id) {
        Tag tag = getById(id);
        tagRepository.delete(tag);
    }
}