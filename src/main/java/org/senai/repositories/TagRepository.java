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

    public List<Tag> findAllById(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return find("id IN ?1", ids).list();
    }

    public Optional<Tag> findByNome(String nome) {
        return find("nome", nome).firstResultOptional();
    }

    @Transactional
    public void removerAssociacoesColaboradorTags(Long tagId) {
        getEntityManager()
                .createNativeQuery("DELETE FROM colaborador_tags WHERE tag_id = ?1")
                .setParameter(1, tagId)
                .executeUpdate();
    }
}
