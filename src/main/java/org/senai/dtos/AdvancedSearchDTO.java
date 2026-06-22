package org.senai.dtos;

import java.time.LocalDate;
import java.util.List;

public record AdvancedSearchDTO(
    List<String> tokens,
    Long supervisorId,
    LocalDate dataAdmissaoInicio,
    LocalDate dataAdmissaoFim
) {}
