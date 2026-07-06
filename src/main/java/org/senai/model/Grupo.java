package org.senai.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "grupos")
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 1500, message = "O nome não pode ter mais de 100 caracteres")
    private String nome;

    @ElementCollection
    @CollectionTable(name = "grupo_tokens", joinColumns = @JoinColumn(name = "grupo_id"))
    @OrderColumn(name = "token_ordem")
    @Column(name = "token")
    private List<String> tokens = new ArrayList<>();

    // Filtros adicionais opcionais
    @ElementCollection
    @CollectionTable(name = "grupo_supervisores", joinColumns = @JoinColumn(name = "grupo_id"))
    @Column(name = "supervisor_id")
    private List<Long> supervisorIds = new ArrayList<>();

    @Column(name = "data_admissao_inicio")
    private LocalDate dataAdmissaoInicio;

    @Column(name = "data_admissao_fim")
    private LocalDate dataAdmissaoFim;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    private Boolean ativo = true;
    private Boolean inativadoPorSistema = false;
    private String motivoInativacao;

    @PrePersist
    void prePersist() {
        this.criadoEm = LocalDateTime.now();
    }

}