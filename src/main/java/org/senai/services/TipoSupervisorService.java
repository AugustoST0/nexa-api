package org.senai.services;

import org.senai.exception.exceptions.RegisterNotFoundException;
import org.senai.model.TipoSupervisor;
import org.senai.repositories.TipoSupervisorRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class TipoSupervisorService {

    @Inject
    TipoSupervisorRepository tipoSupervisorRepository;

    public List<TipoSupervisor> getAll() {
        return tipoSupervisorRepository.listAll();
    }

    public TipoSupervisor getById(Long id) {
        TipoSupervisor tipoSupervisor = tipoSupervisorRepository.findById(id);

        if (tipoSupervisor == null) {
            throw new RegisterNotFoundException("Tipo de Supervisor não encontrado");
        }

        return tipoSupervisor;
    }

    @Transactional
    public TipoSupervisor create(TipoSupervisor tipoSupervisor) {
        return tipoSupervisorRepository.save(tipoSupervisor);
    }

    @Transactional
    public TipoSupervisor update(Long id, TipoSupervisor updatedTipoSupervisor) {
        TipoSupervisor tipoSupervisor = getById(id);

        if (updatedTipoSupervisor.getNome() != null) {
            tipoSupervisor.setNome(updatedTipoSupervisor.getNome());
        }

        if (updatedTipoSupervisor.getDescricao() != null) {
            tipoSupervisor.setDescricao(updatedTipoSupervisor.getDescricao());
        }

        return tipoSupervisorRepository.save(tipoSupervisor);
    }

    @Transactional
    public void delete(Long id) {
        TipoSupervisor tipoSupervisor = getById(id);
        tipoSupervisorRepository.deleteById(id);
    }
}