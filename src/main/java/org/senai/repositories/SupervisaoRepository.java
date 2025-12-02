package org.senai.repositories;

import org.senai.model.Supervisao;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class SupervisaoRepository implements PanacheRepository<Supervisao> {

    @Transactional
    public Supervisao save(Supervisao supervisao) {
        persistAndFlush(supervisao);
        return supervisao;
    }

    public List<Supervisao> findSupervisionadosPorSupervisor(Long supervisorId) {
        return find("supervisor.id = ?1 AND dataFim IS NULL", supervisorId).list();
    }

    public List<Supervisao> findSupervisoresPorColaborador(Long colaboradorId) {
        return find("supervisionado.id = ?1 AND dataFim IS NULL", colaboradorId).list();
    }

    public List<Supervisao> findSupervisionadosPorSupervisorETipo(Long supervisorId, Long tipoSupervisorId) {
        return find("supervisor.id = ?1 AND tipoSupervisor.id = ?2 AND dataFim IS NULL",
                supervisorId, tipoSupervisorId).list();
    }

    public List<Supervisao> findSupervisionadosPorTipo(Long tipoSupervisorId) {
        return find("tipoSupervisor.id = ?1 AND dataFim IS NULL", tipoSupervisorId).list();
    }

    public List<Supervisao> findHistoricoSupervisionado(Long colaboradorId) {
        return find("supervisionado.id = ?1 ORDER BY dataInicio DESC", colaboradorId).list();
    }

    public List<Supervisao> findHistoricoSupervisor(Long supervisorId) {
        return find("supervisor.id = ?1 ORDER BY dataInicio DESC", supervisorId).list();
    }

    public Supervisao findSupervisaoAtiva(Long supervisorId, Long supervisionadoId, Long tipoSupervisorId) {
        return find("supervisor.id = ?1 AND supervisionado.id = ?2 AND tipoSupervisor.id = ?3 AND dataFim IS NULL",
                supervisorId, supervisionadoId, tipoSupervisorId).firstResult();
    }
}