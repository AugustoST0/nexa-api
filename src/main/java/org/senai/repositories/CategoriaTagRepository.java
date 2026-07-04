package org.senai.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.senai.model.CategoriaTag;

import java.util.Optional;

@ApplicationScoped
public class CategoriaTagRepository implements PanacheRepository<CategoriaTag> {

    @Transactional
    public CategoriaTag save(CategoriaTag categoria) {
        persistAndFlush(categoria);
        return categoria;
    }

    public Optional<CategoriaTag> findByNome(String nome) {
        return find("nome", nome).firstResultOptional();
    }
}
