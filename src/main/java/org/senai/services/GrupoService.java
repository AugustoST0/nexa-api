package org.senai.services;

import org.senai.exception.exceptions.RegisterNotFoundException;
import org.senai.model.Grupo;
import org.senai.repositories.GrupoRepository;
import org.senai.repositories.RelatorioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class GrupoService {

    @Inject
    GrupoRepository grupoRepository;

    @Inject
    RelatorioRepository relatorioRepository;

    public List<Grupo> getAll() {
        return grupoRepository.listAll();
    }

    public Grupo getById(Long id) {
        Grupo grupo = grupoRepository.findById(id);

        if (grupo == null) {
            throw new RegisterNotFoundException("Grupo não encontrado");
        }

        return grupo;
    }

    @Transactional
    public Grupo create(Grupo grupo) {
        return grupoRepository.save(grupo);
    }

    @Transactional
    public Grupo update(Long id, Grupo updatedGrupo) {
        Grupo grupo = getById(id);

        if (updatedGrupo.getNome() != null) {
            grupo.setNome(updatedGrupo.getNome());
        }

        if (updatedGrupo.getTokens() != null) {
            grupo.setTokens(updatedGrupo.getTokens());
        }

        return grupoRepository.save(grupo);
    }

    @Transactional
    public void delete(Long id) {
        getById(id);
        // Desvincula relatórios já gerados (mantém o histórico) antes de remover o grupo,
        // evitando violação da FK relatorios.grupo_id
        relatorioRepository.update("grupo = null where grupo.id = ?1", id);
        grupoRepository.deleteById(id);
    }
}
