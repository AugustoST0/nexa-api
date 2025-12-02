package org.senai.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "supervisoes")
public class Supervisao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O supervisor é obrigatório")
    @ManyToOne
    @JoinColumn(name = "supervisor_id", nullable = false)
    private Colaborador supervisor;

    @NotNull(message = "O supervisionado é obrigatório")
    @ManyToOne
    @JoinColumn(name = "supervisionado_id", nullable = false)
    private Colaborador supervisionado;

    @NotNull(message = "O tipo de supervisão é obrigatório")
    @ManyToOne
    @JoinColumn(name = "tipo_supervisor_id", nullable = false)
    private TipoSupervisor tipoSupervisor;

    @NotNull(message = "A data de início é obrigatória")
    private LocalDate dataInicio;

    private LocalDate dataFim; // null = supervisão ativa

    @Size(max = 500)
    private String observacoes;
}