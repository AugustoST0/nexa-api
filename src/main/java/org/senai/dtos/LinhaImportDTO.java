package org.senai.dtos;

import java.util.List;
import java.util.Map;

public record LinhaImportDTO(
        int numeroLinha,
        Map<String, String> dados,
        List<String> erros
) {}
