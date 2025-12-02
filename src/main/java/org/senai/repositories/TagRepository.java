package org.senai.repositories;

import org.senai.model.Tag;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class TagRepository implements PanacheRepository<Tag> {

    @Transactional
    public Tag save(Tag tag) {
        persistAndFlush(tag);
        return tag;
    }

    public List<Tag> findTagsNotInGrupo(Long grupoId) {
        return find(
                "SELECT t FROM Tag t WHERE t.id NOT IN " +
                        "(SELECT t2.id FROM Grupo g JOIN g.tags t2 WHERE g.id = ?1)",
                grupoId
        ).list();
    }

    public List<Tag> findAllById(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return find("id IN ?1", ids).list();
    }

    public Optional<Tag> findByNome(String nome) {
        return find("nome", nome).firstResultOptional();
    }
}
