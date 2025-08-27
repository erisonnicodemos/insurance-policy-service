package com.itau.seguros.solicitacao.api.dto;

import com.itau.seguros.solicitacao.domain.model.CategoriaSeguro;
import com.itau.seguros.solicitacao.domain.model.StatusSolicitacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO para resposta com dados de uma solicitação de apólice.
 * 
 * Contém todas as informações da solicitação, incluindo histórico
 * de mudanças de status para auditoria e acompanhamento.
 */
public record SolicitacaoResponseDTO(
    UUID id,
    UUID customerId,
    String productId,
    CategoriaSeguro category,
    String salesChannel,
    String paymentMethod,
    StatusSolicitacao status,
    LocalDateTime createdAt,
    LocalDateTime finishedAt,
    BigDecimal totalMonthlyPremiumAmount,
    BigDecimal insuredAmount,
    Map<String, BigDecimal> coverages,
    List<String> assistances,
    List<HistoricoStatusDTO> history
) {
    
    /**
     * DTO aninhado para representar o histórico de mudanças de status.
     */
    public record HistoricoStatusDTO(
        StatusSolicitacao status,
        LocalDateTime timestamp,
        String observacao
    ) {}
}

