package com.itau.seguros.solicitacao.domain.event;

import com.itau.seguros.solicitacao.domain.model.CategoriaSeguro;
import com.itau.seguros.solicitacao.domain.model.StatusSolicitacao;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Evento publicado quando uma nova solicitação de apólice é recebida.
 * 
 * Este evento dispara o processo de validação com a API de Fraudes
 * e aplicação das regras de negócio.
 */
public class SolicitacaoRecebidaEvent extends SolicitacaoEvent {
    
    private static final String EVENT_TYPE = "SOLICITACAO_RECEBIDA";
    
    /**
     * Construtor para evento de solicitação recebida.
     * 
     * @param solicitacaoId ID da solicitação
     * @param customerId ID do cliente
     * @param productId ID do produto
     * @param category categoria do seguro
     * @param insuredAmount valor do capital segurado
     */
    public SolicitacaoRecebidaEvent(UUID solicitacaoId, UUID customerId, String productId,
                                   CategoriaSeguro category, BigDecimal insuredAmount) {
        super(solicitacaoId, customerId, productId, category, StatusSolicitacao.RECEBIDO, insuredAmount);
    }
    
    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }
}

