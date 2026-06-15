package org.senai.repositories;

import org.senai.model.Relatorio;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class RelatorioRepository implements PanacheRepository<Relatorio> {

    @Transactional
    public Relatorio save(Relatorio relatorio) {
        persistAndFlush(relatorio);
        return relatorio;
    }
}
