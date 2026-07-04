package org.senai.dtos;

import java.util.List;

public record AtribuicaoTagMassaDTO(Long tagId, List<Long> colaboradorIds) {}
