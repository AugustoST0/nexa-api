package org.senai.dtos;

public record SimpleSearchDTO(
    String nome,
    String matricula,
    String email,
    String cpf,
    String cargo
) {}
