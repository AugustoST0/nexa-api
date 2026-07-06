package org.senai.services;

import org.senai.dtos.ImpactoExclusaoDTO;
import org.senai.dtos.TagCreateDTO;
import org.senai.dtos.TagUpdateDTO;
import org.senai.exception.exceptions.RegisterNotFoundException;
import org.senai.model.CategoriaTag;
import org.senai.model.Grupo;
import org.senai.model.Tag;
import org.senai.repositories.CategoriaTagRepository;
import org.senai.repositories.GrupoRepository;
import org.senai.repositories.TagRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class TagService {

    private static final Set<String> OPERADORES = Set.of("E", "OU", "NÃO POSSUI");

    @Inject
    TagRepository tagRepository;

    @Inject
    CategoriaTagRepository categoriaTagRepository;

    @Inject
    GrupoRepository grupoRepository;

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

        if (dto.categoriaId() != null) {
            CategoriaTag categoria = categoriaTagRepository.findById(dto.categoriaId());
            tag.setCategoria(categoria);
        }

        Tag saved = tagRepository.save(tag);
        tentarReativarGruposAfetados(saved.getNome());
        return saved;
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

        if (dto.categoriaId() != null) {
            CategoriaTag categoria = categoriaTagRepository.findById(dto.categoriaId());
            tag.setCategoria(categoria);
        }

        return tag;
    }

    public ImpactoExclusaoDTO getImpactoExclusao(Long id) {
        Tag tag = getById(id);
        List<String> nomesPesquisasAfetadas = gruposAtivosAfetados(tag).stream()
                .map(Grupo::getNome)
                .toList();

        return new ImpactoExclusaoDTO(nomesPesquisasAfetadas);
    }

    @Transactional
    public void delete(Long id) {
        Tag tag = getById(id);

        List<Grupo> gruposAfetados = gruposAtivosAfetados(tag);
        for (Grupo grupo : gruposAfetados) {
            grupo.setAtivo(false);
            grupo.setInativadoPorSistema(true);
            grupo.setMotivoInativacao("Tag '" + tag.getNome() + "' foi excluída em " + LocalDate.now());
        }

        tagRepository.removerAssociacoesColaboradorTags(id);
        tagRepository.delete(tag);
    }

    private List<Grupo> gruposAtivosAfetados(Tag tag) {
        return grupoRepository.findAtivosPorToken(tag.getNome());
    }

    private void tentarReativarGruposAfetados(String nomeTagCriada) {
        String marcador = "Tag '" + nomeTagCriada + "'";

        List<Grupo> candidatos = grupoRepository.findInativadosPorSistema().stream()
                .filter(grupo -> grupo.getMotivoInativacao() != null && grupo.getMotivoInativacao().contains(marcador))
                .toList();

        for (Grupo grupo : candidatos) {
            boolean todasTagsExistem = grupo.getTokens().stream()
                    .filter(token -> !OPERADORES.contains(token))
                    .allMatch(token -> tagRepository.findByNome(token).isPresent());

            if (todasTagsExistem) {
                grupo.setAtivo(true);
                grupo.setInativadoPorSistema(false);
                grupo.setMotivoInativacao(null);
            }
        }
    }
}