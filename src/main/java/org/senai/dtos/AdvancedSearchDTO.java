package org.senai.dtos;

import java.time.LocalDate;
import java.util.List;

public record AdvancedSearchDTO(
    List<String> tokens,
    List<Long> supervisorIds,
    LocalDate dataAdmissaoInicio,
    LocalDate dataAdmissaoFim
) {}
