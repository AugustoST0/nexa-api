package org.senai.services;

import org.senai.dtos.MigrarSupervisionadosDTO;
import org.senai.dtos.TrocarSupervisorDTO;
import org.senai.exception.exceptions.RegisterNotFoundException;
import org.senai.model.Colaborador;
import org.senai.model.Supervisao;
import org.senai.model.TipoSupervisor;
import org.senai.repositories.SupervisaoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class SupervisaoService {

    @Inject
    SupervisaoRepository supervisaoRepository;

    @Inject
    ColaboradorService colaboradorService;

    @Inject
    TipoSupervisorService tipoSupervisorService;

    public List<Supervisao> getAll() {
        return supervisaoRepository.listAll();
    }

    public Supervisao getById(Long id) {
        Supervisao supervisao = supervisaoRepository.findById(id);

        if (supervisao == null) {
            throw new RegisterNotFoundException("Supervisão não encontrada");
        }

        return supervisao;
    }

    public List<Supervisao> getSupervisionadosPorSupervisor(Long supervisorId) {
        return supervisaoRepository.findSupervisionadosPorSupervisor(supervisorId);
    }

    public List<Supervisao> getSupervisoresPorColaborador(Long colaboradorId) {
        return supervisaoRepository.findSupervisoresPorColaborador(colaboradorId);
    }

    public List<Supervisao> getSupervisionadosPorSupervisorETipo(Long supervisorId, Long tipoSupervisorId) {
        return supervisaoRepository.findSupervisionadosPorSupervisorETipo(supervisorId, tipoSupervisorId);
    }

    public List<Supervisao> getSupervisionadosPorTipo(Long tipoSupervisorId) {
        return supervisaoRepository.findSupervisionadosPorTipo(tipoSupervisorId);
    }

    public List<Supervisao> getHistoricoSupervisionado(Long colaboradorId) {
        return supervisaoRepository.findHistoricoSupervisionado(colaboradorId);
    }

    public List<Supervisao> getHistoricoSupervisor(Long supervisorId) {
        return supervisaoRepository.findHistoricoSupervisor(supervisorId);
    }

    public List<Supervisao> getSupervisoesAtivasPorColaborador(Long colaboradorId) {
        return supervisaoRepository.find(
                "supervisionado.id = ?1 AND dataFim IS NULL",
                colaboradorId
        ).list();
    }

    @Transactional
    public Supervisao create(Supervisao supervisao) {
        Colaborador supervisor = colaboradorService.getById(supervisao.getSupervisor().getId());
        Colaborador supervisionado = colaboradorService.getById(supervisao.getSupervisionado().getId());
        TipoSupervisor tipoSupervisor = tipoSupervisorService.getById(supervisao.getTipoSupervisor().getId());

        if (supervisao.getDataInicio() == null) {
            supervisao.setDataInicio(LocalDate.now());
        }

        return supervisaoRepository.save(supervisao);
    }

    @Transactional
    public Supervisao update(Long id, Supervisao updatedSupervisao) {
        Supervisao supervisao = getById(id);

        if (updatedSupervisao.getDataFim() != null) {
            supervisao.setDataFim(updatedSupervisao.getDataFim());
        }

        if (updatedSupervisao.getObservacoes() != null) {
            supervisao.setObservacoes(updatedSupervisao.getObservacoes());
        }

        return supervisaoRepository.save(supervisao);
    }

    @Transactional
    public void delete(Long id) {
        Supervisao supervisao = getById(id);
        supervisaoRepository.deleteById(id);
    }

    @Transactional
    public void encerrarSupervisao(Long id) {
        Supervisao supervisao = getById(id);
        supervisao.setDataFim(LocalDate.now());

        String observacaoAtual = supervisao.getObservacoes() != null ? supervisao.getObservacoes() + " | " : "";
        supervisao.setObservacoes(observacaoAtual + "Encerrado em " + LocalDate.now());

        supervisaoRepository.save(supervisao);
    }

    @Transactional
    public void trocarSupervisor(TrocarSupervisorDTO dto) {
        Colaborador colaborador = colaboradorService.getById(dto.colaboradorId());
        Colaborador novoSupervisor = colaboradorService.getById(dto.novoSupervisorId());
        TipoSupervisor tipoSupervisor = tipoSupervisorService.getById(dto.tipoSupervisorId());

        List<Supervisao> supervisionesAtivas = supervisaoRepository.find(
                "supervisionado.id = ?1 AND tipoSupervisor.id = ?2 AND dataFim IS NULL",
                dto.colaboradorId(), dto.tipoSupervisorId()
        ).list();

        LocalDate hoje = LocalDate.now();

        for (Supervisao supervisao : supervisionesAtivas) {
            supervisao.setDataFim(hoje);
            String observacaoAtual = supervisao.getObservacoes() != null ? supervisao.getObservacoes() + " | " : "";
            supervisao.setObservacoes(observacaoAtual + "Trocado para " + novoSupervisor.getNome() +
                    (dto.motivo() != null ? ". Motivo: " + dto.motivo() : ""));
        }

        Supervisao novaSupervisao = new Supervisao();
        novaSupervisao.setSupervisor(novoSupervisor);
        novaSupervisao.setSupervisionado(colaborador);
        novaSupervisao.setTipoSupervisor(tipoSupervisor);
        novaSupervisao.setDataInicio(hoje);
        novaSupervisao.setObservacoes(dto.motivo());

        supervisaoRepository.persist(novaSupervisao);
    }

    @Transactional
    public void migrarTodosSupervisionados(MigrarSupervisionadosDTO dto) {
        Colaborador supervisorAntigo = colaboradorService.getById(dto.supervisorAntigoId());
        Colaborador supervisorNovo = colaboradorService.getById(dto.supervisorNovoId());

        String query = "supervisor.id = ?1 AND dataFim IS NULL";
        List<Object> params = List.of(dto.supervisorAntigoId());

        if (dto.tipoSupervisorId() != null) {
            tipoSupervisorService.getById(dto.tipoSupervisorId());
            query += " AND tipoSupervisor.id = ?2";
            params = List.of(dto.supervisorAntigoId(), dto.tipoSupervisorId());
        }

        List<Supervisao> supervisionesAtivas = supervisaoRepository.find(query, params.toArray()).list();
        migrarSupervisoes(supervisionesAtivas, supervisorAntigo, supervisorNovo, dto.motivo());
    }

    @Transactional
    public void migrarSupervisionadosSeletivo(MigrarSupervisionadosDTO dto) {
        if (dto.colaboradorIds() == null || dto.colaboradorIds().isEmpty()) {
            throw new IllegalArgumentException("Lista de colaboradores não pode estar vazia");
        }

        Colaborador supervisorAntigo = colaboradorService.getById(dto.supervisorAntigoId());
        Colaborador supervisorNovo = colaboradorService.getById(dto.supervisorNovoId());

        for (Long colaboradorId : dto.colaboradorIds()) {
            List<Supervisao> supervisionesAtivas = supervisaoRepository.find(
                    "supervisor.id = ?1 AND supervisionado.id = ?2 AND dataFim IS NULL",
                    dto.supervisorAntigoId(), colaboradorId
            ).list();

            migrarSupervisoes(supervisionesAtivas, supervisorAntigo, supervisorNovo, dto.motivo());
        }
    }

    private void migrarSupervisoes(List<Supervisao> supervisionesAtivas, Colaborador supervisorAntigo,
                                   Colaborador supervisorNovo, String motivo) {
        LocalDate hoje = LocalDate.now();
        String motivoTexto = motivo != null ? ". Motivo: " + motivo : "";

        for (Supervisao supervisaoAntiga : supervisionesAtivas) {
            supervisaoAntiga.setDataFim(hoje);
            String observacaoAtual = supervisaoAntiga.getObservacoes() != null ? supervisaoAntiga.getObservacoes() + " | " : "";
            supervisaoAntiga.setObservacoes(observacaoAtual + "Migrado para " + supervisorNovo.getNome() + motivoTexto);

            Supervisao novaSupervisao = new Supervisao();
            novaSupervisao.setSupervisor(supervisorNovo);
            novaSupervisao.setSupervisionado(supervisaoAntiga.getSupervisionado());
            novaSupervisao.setTipoSupervisor(supervisaoAntiga.getTipoSupervisor());
            novaSupervisao.setDataInicio(hoje);
            novaSupervisao.setObservacoes("Migrado de " + supervisorAntigo.getNome() + motivoTexto);

            supervisaoRepository.persist(novaSupervisao);
        }
    }
}