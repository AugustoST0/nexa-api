package org.senai.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.senai.dtos.CategoriaTagDTO;
import org.senai.exception.exceptions.BusinessRuleException;
import org.senai.exception.exceptions.RegisterNotFoundException;
import org.senai.model.CategoriaTag;
import org.senai.repositories.CategoriaTagRepository;
import org.senai.repositories.TagRepository;

import java.util.List;

@ApplicationScoped
public class CategoriaTagService {

    @Inject
    CategoriaTagRepository categoriaTagRepository;

    @Inject
    TagRepository tagRepository;

    public List<CategoriaTag> getAll() {
        return categoriaTagRepository.listAll();
    }

    public CategoriaTag getById(Long id) {
        CategoriaTag categoria = categoriaTagRepository.findById(id);
        if (categoria == null) {
            throw new RegisterNotFoundException("Categoria não encontrada");
        }
        return categoria;
    }

    @Transactional
    public CategoriaTag create(CategoriaTagDTO dto) {
        CategoriaTag categoria = new CategoriaTag();
        categoria.setNome(dto.nome());
        categoria.setCor(dto.cor());
        return categoriaTagRepository.save(categoria);
    }

    @Transactional
    public CategoriaTag update(Long id, CategoriaTagDTO dto) {
        CategoriaTag categoria = getById(id);
        if (dto.nome() != null) {
            categoria.setNome(dto.nome());
        }
        if (dto.cor() != null) {
            categoria.setCor(dto.cor());
        }
        return categoria;
    }

    @Transactional
    public void delete(Long id) {
        CategoriaTag categoria = getById(id);
        long count = tagRepository.count("categoria.id", id);
        if (count > 0) {
            throw new BusinessRuleException(
                "Não é possível excluir a categoria: existem " + count + " tag(s) vinculada(s)"
            );
        }
        categoriaTagRepository.delete(categoria);
    }
}
