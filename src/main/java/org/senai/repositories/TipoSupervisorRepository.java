package org.senai.repositories;

import org.senai.model.TipoSupervisor;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class TipoSupervisorRepository implements PanacheRepository<TipoSupervisor> {

    @Transactional
    public TipoSupervisor save(TipoSupervisor tipoSupervisor) {
        persistAndFlush(tipoSupervisor);
        return tipoSupervisor;
    }
}