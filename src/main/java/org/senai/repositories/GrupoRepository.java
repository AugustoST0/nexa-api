package org.senai.repositories;

import org.senai.model.Grupo;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.Optional;

@ApplicationScoped
public class GrupoRepository implements PanacheRepository<Grupo> {

    @Transactional
    public Grupo save(Grupo grupo) {
        persistAndFlush(grupo);
        return grupo;
    }

    public Optional<Grupo> findByNome(String nome) {
        return find("nome", nome).firstResultOptional();
    }
}
