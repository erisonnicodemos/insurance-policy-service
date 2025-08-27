package com.itau.seguros.solicitacao.domain.event;

import com.itau.seguros.solicitacao.domain.model.CategoriaSeguro;
import com.itau.seguros.solicitacao.domain.model.StatusSolicitacao;
import com.itau.seguros.solicitacao.domain.model.TipoCliente;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Evento publicado quando uma solicitação de apólice é validada
 * com sucesso pela API de Fraudes e regras de negócio.
 * 
 * Este evento indica que a solicitação pode prosseguir para
 * o estado pendente, aguardando pagamento e subscrição.
 */
public class SolicitacaoValidadaEvent extends SolicitacaoEvent {
    
    private static final String EVENT_TYPE = "SOLICITACAO_VALIDADA";
    
    private final TipoCliente tipoCliente;
    
    /**
     * Construtor para evento de solicitação validada.
     * 
     * @param solicitacaoId ID da solicitação
     * @param customerId ID do cliente
     * @param productId ID do produto
     * @param category categoria do seguro
     * @param insuredAmount valor do capital segurado
     * @param tipoCliente classificação de risco do cliente
     */
    public SolicitacaoValidadaEvent(UUID solicitacaoId, UUID customerId, String productId,
                                   CategoriaSeguro category, BigDecimal insuredAmount,
                                   TipoCliente tipoCliente) {
        super(solicitacaoId, customerId, productId, category, StatusSolicitacao.VALIDADO, insuredAmount);
        this.tipoCliente = tipoCliente;
    }
    
    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }
    
    public TipoCliente getTipoCliente() {
        return tipoCliente;
    }
}

