package org.senai.services;

import org.hibernate.Hibernate;
import org.senai.dtos.AtribuirSupervisorDTO;
import org.senai.dtos.ColaboradorCreateUpdateDTO;
import org.senai.dtos.ColaboradorFilterResponseDTO;
import org.senai.dtos.SupervisorDiretoDTO;
import org.senai.exception.exceptions.BusinessRuleException;
import org.senai.exception.exceptions.RegisterNotFoundException;
import org.senai.model.Colaborador;
import org.senai.model.Supervisao;
import org.senai.model.Tag;
import org.senai.model.TipoSupervisor;
import org.senai.repositories.ColaboradorRepository;
import org.senai.utils.ValidationUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class ColaboradorService {

    @Inject
    ColaboradorRepository colaboradorRepository;

    @Inject
    TagService tagService;

    @Inject
    SupervisaoService supervisaoService;

    @Inject
    TipoSupervisorService tipoSupervisorService;

    @Transactional
    public List<Colaborador> getAll() {
        List<Colaborador> colaboradores = colaboradorRepository.listAll();

        for (Colaborador colaborador : colaboradores) {
            // Inicializa as tags ainda dentro da transação (a serialização JSON ocorre depois)
            colaborador.getTags().size();

            // Busca explícita das supervisões ativas (supervisionado.id = colaborador e dataFim IS NULL)
            // em vez de confiar na coleção lazy mappedBy — garante leitura fresca dos dados commitados.
            List<Supervisao> supervisoesAtivas =
                    supervisaoService.getSupervisoesAtivasPorColaborador(colaborador.getId());

            supervisoesAtivas.stream()
                    .min(Comparator.comparingInt(s ->
                            s.getTipoSupervisor().getNivel() != null
                                    ? s.getTipoSupervisor().getNivel()
                                    : Integer.MAX_VALUE))
                    .ifPresent(s -> colaborador.setSupervisor(new SupervisorDiretoDTO(
                            s.getSupervisor().getId(),
                            s.getSupervisor().getNome(),
                            s.getTipoSupervisor().getNome()
                    )));
        }

        return colaboradores;
    }

    public Colaborador getById(Long id) {
        Colaborador colaborador = colaboradorRepository.findById(id);

        if (colaborador == null) {
            throw new RegisterNotFoundException("Colaborador não encontrado");
        }

        return colaborador;
    }

    public Colaborador getByMatricula(String matricula) {
        return colaboradorRepository.findByMatricula(matricula)
                .orElseThrow(() -> new RegisterNotFoundException("Colaborador com matrícula " + matricula + " não encontrado"));
    }

    public Colaborador getByEmail(String email) {
        return colaboradorRepository.findByEmail(email)
                .orElseThrow(() -> new RegisterNotFoundException("Colaborador com email " + email + " não encontrado"));
    }

    public Colaborador getByCpf(String cpf) {
        return colaboradorRepository.findByCpf(cpf)
                .orElseThrow(() -> new RegisterNotFoundException("Colaborador com CPF " + cpf + " não encontrado"));
    }

    public List<Colaborador> getSupervisores() {
        return colaboradorRepository.findSupervisores();
    }

    public List<Colaborador> getByTag(Long tagId) {
        try {
            if (tagId == null || tagId <= 0) {
                throw new IllegalArgumentException("ID da tag deve ser um número positivo");
            }
            
            // Verify tag exists before searching
            Tag tag = tagService.getById(tagId);
            if (tag == null) {
                throw new RegisterNotFoundException("Tag com ID " + tagId + " não encontrada");
            }
            
            return colaboradorRepository.findByTag(tagId);
        } catch (RegisterNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar colaboradores por tag: " + e.getMessage(), e);
        }
    }

    public List<Colaborador> searchByNome(String nome) {
        return colaboradorRepository.findByNomeContaining(nome);
    }

    @Transactional
    public Colaborador create(ColaboradorCreateUpdateDTO dto) {
        Colaborador colaborador = new Colaborador();
        colaborador.setNome(dto.nome());
        colaborador.setMatricula(dto.matricula());
        colaborador.setEmail(dto.email());
        colaborador.setCpf(dto.cpf());
        colaborador.setDataNascimento(dto.dataNascimento());
        colaborador.setDataAdmissao(dto.dataAdmissao());
        colaborador.setCargo(dto.cargo());
        colaborador.setDepartamento(dto.departamento());

        if (dto.tagIds() != null && !dto.tagIds().isEmpty()) {
            List<Tag> tags = tagService.findAllById(dto.tagIds());
            colaborador.setTags(tags);
        }

        Colaborador saved = colaboradorRepository.save(colaborador);
        Hibernate.initialize(saved.getTags());

        return saved;
    }

    @Transactional
    public Colaborador update(Long id, ColaboradorCreateUpdateDTO dto) {
        Colaborador colaborador = getById(id);

        if (dto.nome() != null) {
            colaborador.setNome(dto.nome());
        }

        if (dto.matricula() != null) {
            colaborador.setMatricula(dto.matricula());
        }

        if (dto.email() != null) {
            colaborador.setEmail(dto.email());
        }

        if (dto.cpf() != null) {
            colaborador.setCpf(dto.cpf());
        }

        if (dto.dataNascimento() != null) {
            colaborador.setDataNascimento(dto.dataNascimento());
        }

        if (dto.dataAdmissao() != null) {
            colaborador.setDataAdmissao(dto.dataAdmissao());
        }

        if (dto.cargo() != null) {
            colaborador.setCargo(dto.cargo());
        }

        if (dto.departamento() != null) {
            colaborador.setDepartamento(dto.departamento());
        }

        if (dto.tagIds() != null) {
            List<Tag> tags = tagService.findAllById(dto.tagIds());
            colaborador.getTags().clear();
            colaborador.getTags().addAll(tags);
        }

        Colaborador saved = colaboradorRepository.save(colaborador);

        Hibernate.initialize(saved.getTags());

        return saved;
    }

    @Transactional
    public void delete(Long id) {
        Colaborador colaborador = getById(id);
        colaborador.getTags().clear();
        colaboradorRepository.deleteById(id);
    }

    @Transactional
    public void linkToTag(Long colaboradorId, Long tagId) {
        try {
            if (colaboradorId == null || colaboradorId <= 0) {
                throw new IllegalArgumentException("ID do colaborador deve ser um número positivo");
            }
            if (tagId == null || tagId <= 0) {
                throw new IllegalArgumentException("ID da tag deve ser um número positivo");
            }
            
            Colaborador colaborador = getById(colaboradorId);
            Tag tag = tagService.getById(tagId);

            // Ensure collections are initialized
            if (colaborador.getTags() == null) {
                colaborador.setTags(new java.util.ArrayList<>());
            }

            if (!colaborador.getTags().contains(tag)) {
                colaborador.getTags().add(tag);
                colaboradorRepository.save(colaborador);
            }
        } catch (RegisterNotFoundException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao vincular tag ao colaborador: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void unlinkFromTag(Long colaboradorId, Long tagId) {
        try {
            if (colaboradorId == null || colaboradorId <= 0) {
                throw new IllegalArgumentException("ID do colaborador deve ser um número positivo");
            }
            if (tagId == null || tagId <= 0) {
                throw new IllegalArgumentException("ID da tag deve ser um número positivo");
            }
            
            Colaborador colaborador = getById(colaboradorId);
            Tag tag = tagService.getById(tagId);

            // Ensure collections are initialized
            if (colaborador.getTags() != null) {
                colaborador.getTags().remove(tag);
                colaboradorRepository.save(colaborador);
            }
        } catch (RegisterNotFoundException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao desvincular tag do colaborador: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void atribuirSupervisor(Long colaboradorId, AtribuirSupervisorDTO dto) {
        Colaborador colaborador = getById(colaboradorId);
        Colaborador supervisor = getById(dto.supervisorId());
        TipoSupervisor tipoSupervisor = tipoSupervisorService.getById(dto.tipoSupervisorId());

        if (colaboradorId.equals(dto.supervisorId())) {
            throw new BusinessRuleException("Um colaborador não pode ser supervisor dele mesmo");
        }

        List<Supervisao> supervisoesAtivas = supervisaoService.getSupervisoesAtivasPorColaborador(colaboradorId);

        LocalDate hoje = LocalDate.now();

        for (Supervisao supervisaoAtiva : supervisoesAtivas) {
            supervisaoAtiva.setDataFim(hoje);
            String observacaoAtual = supervisaoAtiva.getObservacoes() != null ? supervisaoAtiva.getObservacoes() + " | " : "";
            supervisaoAtiva.setObservacoes(observacaoAtual + "Substituído por nova supervisão");
        }

        Supervisao novaSupervisao = new Supervisao();
        novaSupervisao.setSupervisor(supervisor);
        novaSupervisao.setSupervisionado(colaborador);
        novaSupervisao.setTipoSupervisor(tipoSupervisor);
        novaSupervisao.setDataInicio(hoje);
        novaSupervisao.setObservacoes(dto.observacoes());

        supervisaoService.create(novaSupervisao);
    }

    @Inject
    AdvancedSearchProcessor advancedSearchProcessor;

    public List<ColaboradorFilterResponseDTO> searchCommon(String nome, String matricula, String email, String cpf, String cargo) {
        try {
            // Validate input parameters
            validateSearchParameters(nome, matricula, email, cpf, cargo);
            
            // Sanitize input parameters
            nome = sanitizeInput(nome);
            matricula = sanitizeInput(matricula);
            email = sanitizeInput(email);
            cpf = sanitizeInput(cpf);
            cargo = sanitizeInput(cargo);
            
            List<Colaborador> colaboradores = colaboradorRepository.searchCommon(nome, matricula, email, cpf, cargo);
            
            // Initialize tags for each colaborador to avoid lazy loading issues
            colaboradores.forEach(c -> {
                if (c.getTags() != null) {
                    c.getTags().size(); // Force initialization
                }
            });
            
            return colaboradores.stream()
                .map(ColaboradorFilterResponseDTO::fromEntity)
                .toList();
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Parâmetros de busca inválidos: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro detalhado na busca comum: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro interno ao realizar busca comum: " + e.getMessage(), e);
        }
    }

    public List<ColaboradorFilterResponseDTO> searchAdvanced(List<String> tokens, List<Long> supervisorIds,
                                                             LocalDate dataAdmissaoInicio, LocalDate dataAdmissaoFim) {
        try {
            // Sanitize tokens (podem vir nulos/vazios quando a busca usa apenas filtros extras)
            List<String> sanitizedTokens = tokens == null ? List.of() : tokens.stream()
                .filter(token -> token != null && !token.trim().isEmpty())
                .map(String::trim)
                .toList();

            boolean temFiltros = (supervisorIds != null && !supervisorIds.isEmpty()) || dataAdmissaoInicio != null || dataAdmissaoFim != null;
            if (sanitizedTokens.isEmpty() && !temFiltros) {
                throw new IllegalArgumentException("Pelo menos um critério de busca deve ser fornecido");
            }

            var queryMap = advancedSearchProcessor.buildCombinedQuery(
                    sanitizedTokens.isEmpty() ? null : sanitizedTokens,
                    supervisorIds, dataAdmissaoInicio, dataAdmissaoFim);
            String query = (String) queryMap.get("query");
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> params = (java.util.Map<String, Object>) queryMap.get("params");

            List<Colaborador> colaboradores = colaboradorRepository.searchAdvanced(query, params);
            List<String> relevantTagNames = advancedSearchProcessor.getRelevantTagNames(sanitizedTokens);

            return colaboradores.stream()
                .map(c -> ColaboradorFilterResponseDTO.fromEntityWithFilteredTags(c, relevantTagNames))
                .toList();
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Parâmetros de busca avançada inválidos: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Erro interno ao realizar busca avançada: " + e.getMessage(), e);
        }
    }
    
    private void validateSearchParameters(String nome, String matricula, String email, String cpf, String cargo) {
        // Check if at least one parameter is provided
        ValidationUtils.validateAtLeastOneParameter(nome, matricula, email, cpf, cargo);
        
        // Validate individual parameters
        ValidationUtils.validateSearchParameter("Nome", nome, 255);
        ValidationUtils.validateMatriculaParameter(matricula);
        ValidationUtils.validateEmailParameter(email);
        ValidationUtils.validateCpfParameter(cpf);
        ValidationUtils.validateSearchParameter("Cargo", cargo, 255);
    }
    
    private String sanitizeInput(String input) {
        return ValidationUtils.sanitizeInput(input);
    }
}