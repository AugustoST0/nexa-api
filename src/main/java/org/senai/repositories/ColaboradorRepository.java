package org.senai.repositories;

import org.senai.model.Colaborador;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.QueryTimeoutException;
import jakarta.transaction.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class ColaboradorRepository implements PanacheRepository<Colaborador> {

    @Transactional
    public Colaborador save(Colaborador colaborador) {
        persistAndFlush(colaborador);
        return colaborador;
    }

    public Optional<Colaborador> findByMatricula(String matricula) {
        return find("matricula", matricula).firstResultOptional();
    }

    public Optional<Colaborador> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public Optional<Colaborador> findByCpf(String cpf) {
        return find("cpf", cpf).firstResultOptional();
    }

    public List<Colaborador> findSupervisores() {
        return find("SELECT DISTINCT c FROM Colaborador c JOIN c.supervisionando s WHERE s.dataFim IS NULL").list();
    }

    public List<Colaborador> findByTag(Long tagId) {
        return find("SELECT c FROM Colaborador c JOIN c.tags t WHERE t.id = ?1", tagId).list();
    }

    public List<Colaborador> findByNomeContaining(String nome) {
        return find("LOWER(nome) LIKE LOWER(?1)", "%" + nome + "%").list();
    }

    public boolean existsByMatricula(String matricula) {
        return count("matricula", matricula) > 0;
    }

    public boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }

    public boolean existsByCpf(String cpf) {
        return count("cpf", cpf) > 0;
    }

    public List<Colaborador> searchCommon(String nome, String matricula, String email, String cpf, String cargo) {
        try {
            StringBuilder query = new StringBuilder("SELECT c FROM Colaborador c WHERE 1=1");
            Map<String, Object> params = new HashMap<>();
            
            if (nome != null && !nome.trim().isEmpty()) {
                query.append(" AND LOWER(c.nome) LIKE LOWER(:nome)");
                params.put("nome", "%" + nome.trim() + "%");
            }
            if (matricula != null && !matricula.trim().isEmpty()) {
                query.append(" AND LOWER(c.matricula) LIKE LOWER(:matricula)");
                params.put("matricula", "%" + matricula.trim() + "%");
            }
            if (email != null && !email.trim().isEmpty()) {
                query.append(" AND LOWER(c.email) LIKE LOWER(:email)");
                params.put("email", "%" + email.trim() + "%");
            }
            if (cpf != null && !cpf.trim().isEmpty()) {
                query.append(" AND LOWER(c.cpf) LIKE LOWER(:cpf)");
                params.put("cpf", "%" + cpf.trim() + "%");
            }
            if (cargo != null && !cargo.trim().isEmpty()) {
                query.append(" AND LOWER(c.cargo) LIKE LOWER(:cargo)");
                params.put("cargo", "%" + cargo.trim() + "%");
            }
            
            // Set query timeout (30 seconds) and fetch tags eagerly
            String queryWithJoin = query.toString().replace("SELECT c FROM Colaborador c", 
                "SELECT DISTINCT c FROM Colaborador c LEFT JOIN FETCH c.tags");
            
            var entityQuery = getEntityManager().createQuery(queryWithJoin, Colaborador.class);
            
            // Set parameters
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                entityQuery.setParameter(entry.getKey(), entry.getValue());
            }
            
            return entityQuery
                .setMaxResults(1000) // Limit results to prevent memory issues
                .getResultList();
        } catch (QueryTimeoutException e) {
            throw new RuntimeException("Consulta excedeu o tempo limite. Tente refinar os critérios de busca.", e);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao executar busca comum: " + e.getMessage(), e);
        }
    }

    public List<Colaborador> searchAdvanced(String hqlQuery, java.util.Map<String, Object> params) {
        try {
            var query = getEntityManager().createQuery(hqlQuery, Colaborador.class);
            
            // Set parameters
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }
            
            // Set query timeout and result limit
            return query
                .setMaxResults(1000) // Limit results to prevent memory issues
                .getResultList();
        } catch (QueryTimeoutException e) {
            throw new RuntimeException("Consulta avançada excedeu o tempo limite. Tente simplificar os critérios de busca.", e);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao executar busca avançada: " + e.getMessage(), e);
        }
    }
}