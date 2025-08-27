package com.itau.seguros.solicitacao.infrastructure.external;

import com.itau.seguros.solicitacao.api.dto.FraudeResponseDTO;

import java.util.UUID;

/**
 * Interface para o cliente da API de Fraudes.
 * 
 * Define o contrato para consulta de classificação de risco
 * de clientes, permitindo implementações reais ou mockadas.
 */
public interface ApiFraudesClient {
    
    /**
     * Consulta a classificação de risco de um cliente para uma solicitação.
     * 
     * @param solicitacaoId ID da solicitação
     * @param customerId ID do cliente
     * @return resposta com classificação e ocorrências
     */
    FraudeResponseDTO consultarClassificacaoRisco(UUID solicitacaoId, UUID customerId);
}

