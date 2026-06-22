package org.senai.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.senai.dtos.SupervisorDiretoDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "colaboradores")
public class Colaborador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 100, message = "O nome não pode ter mais de 100 caracteres")
    private String nome;

    @NotBlank(message = "A matrícula é obrigatória")
    @Size(max = 20, message = "A matrícula não pode ter mais de 20 caracteres")
    @Column(unique = true)
    private String matricula;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Email inválido")
    @Size(max = 150, message = "O email não pode ter mais de 150 caracteres")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "O CPF é obrigatório")
    @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{11}", message = "CPF inválido")
    @Column(unique = true)
    private String cpf;

    @NotNull(message = "A data de nascimento é obrigatória")
    @Past(message = "A data de nascimento deve ser no passado")
    private LocalDate dataNascimento;

    @NotNull(message = "A data de admissão é obrigatória")
    @PastOrPresent(message = "A data de admissão não pode ser futura")
    private LocalDate dataAdmissao;

    @NotBlank(message = "O cargo é obrigatório")
    private String cargo;

    @NotBlank(message = "O departamento é obrigatório")
    private String departamento;

    @ManyToMany
    @JoinTable(
            name = "colaborador_tags",
            joinColumns = @JoinColumn(name = "colaborador_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "supervisor")
    @JsonIgnore
    private List<Supervisao> supervisionando = new ArrayList<>();

    @OneToMany(mappedBy = "supervisionado")
    @JsonIgnore
    private List<Supervisao> supervisoes = new ArrayList<>();

    // Supervisor direto (supervisão ativa de menor nível) — populado no ColaboradorService.getAll()
    @Transient
    private SupervisorDiretoDTO supervisor;

    @Transient
    @JsonIgnore
    public boolean isSupervisor() {
        return !supervisionando.isEmpty();
    }
}