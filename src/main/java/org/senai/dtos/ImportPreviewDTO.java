package org.senai.dtos;

import java.util.List;

public record ImportPreviewDTO(
        List<String> colunasDetectadas,
        List<LinhaImportDTO> linhas
) {}
