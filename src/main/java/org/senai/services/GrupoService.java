package org.senai.services;

import org.senai.dtos.GrupoCreateDTO;
import org.senai.exception.exceptions.RegisterNotFoundException;
import org.senai.model.Grupo;
import org.senai.repositories.GrupoRepository;
import org.senai.repositories.RelatorioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
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
    public Grupo create(GrupoCreateDTO dto) {
        Grupo grupo = new Grupo();
        aplicarDados(grupo, dto);
        return grupoRepository.save(grupo);
    }

    @Transactional
    public Grupo update(Long id, GrupoCreateDTO dto) {
        Grupo grupo = getById(id);
        aplicarDados(grupo, dto);
        return grupoRepository.save(grupo);
    }

    private void aplicarDados(Grupo grupo, GrupoCreateDTO dto) {
        grupo.setNome(dto.nome());
        grupo.setTokens(dto.tokens() != null ? new ArrayList<>(dto.tokens()) : new ArrayList<>());
        grupo.setSupervisorIds(dto.supervisorIds() != null ? new ArrayList<>(dto.supervisorIds()) : new ArrayList<>());
        grupo.setDataAdmissaoInicio(dto.dataAdmissaoInicio());
        grupo.setDataAdmissaoFim(dto.dataAdmissaoFim());
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
