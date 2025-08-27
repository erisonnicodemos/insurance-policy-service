package com.itau.seguros.solicitacao.api.dto;

import com.itau.seguros.solicitacao.domain.model.TipoCliente;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO para resposta da API de Fraudes (mockada).
 * 
 * Representa a classificação de risco de um cliente para uma solicitação
 * específica, incluindo histórico de ocorrências anteriores.
 */
public record FraudeResponseDTO(
    UUID orderId,
    UUID customerId,
    LocalDateTime analyzedAt,
    TipoCliente classification,
    List<OcorrenciaDTO> occurrences
) {
    
    /**
     * DTO aninhado para representar ocorrências de fraude ou suspeita.
     */
    public record OcorrenciaDTO(
        UUID id,
        String productId,
        String type,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}
}

