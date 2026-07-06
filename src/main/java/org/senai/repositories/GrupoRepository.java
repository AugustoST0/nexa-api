package org.senai.repositories;

import org.senai.model.Grupo;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
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

    public List<Grupo> findAtivosPorToken(String token) {
        return find("?1 MEMBER OF tokens AND ativo = true", token).list();
    }

    public List<Grupo> findAtivosPorSupervisorId(Long supervisorId) {
        return find("?1 MEMBER OF supervisorIds AND ativo = true", supervisorId).list();
    }

    public List<Grupo> findInativadosPorSistema() {
        return find("inativadoPorSistema = true").list();
    }
}
