package com.itau.seguros.solicitacao.domain.event;

import com.itau.seguros.solicitacao.domain.model.CategoriaSeguro;
import com.itau.seguros.solicitacao.domain.model.StatusSolicitacao;
import com.itau.seguros.solicitacao.domain.model.TipoCliente;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Evento publicado quando uma solicitação de apólice é rejeitada
 * por não atender às regras de validação, falha no pagamento
 * ou negativa na subscrição.
 * 
 * Este evento indica o fim do ciclo de vida da solicitação
 * com status de rejeição.
 */
public class SolicitacaoRejeitadaEvent extends SolicitacaoEvent {
    
    private static final String EVENT_TYPE = "SOLICITACAO_REJEITADA";
    
    private final String motivoRejeicao;
    private final TipoCliente tipoCliente;
    
    /**
     * Construtor para evento de solicitação rejeitada.
     * 
     * @param solicitacaoId ID da solicitação
     * @param customerId ID do cliente
     * @param productId ID do produto
     * @param category categoria do seguro
     * @param insuredAmount valor do capital segurado
     * @param tipoCliente classificação de risco do cliente
     * @param motivoRejeicao motivo da rejeição
     */
    public SolicitacaoRejeitadaEvent(UUID solicitacaoId, UUID customerId, String productId,
                                    CategoriaSeguro category, BigDecimal insuredAmount,
                                    TipoCliente tipoCliente, String motivoRejeicao) {
        super(solicitacaoId, customerId, productId, category, StatusSolicitacao.REJEITADA, insuredAmount);
        this.tipoCliente = tipoCliente;
        this.motivoRejeicao = motivoRejeicao;
    }
    
    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }
    
    public String getMotivoRejeicao() {
        return motivoRejeicao;
    }
    
    public TipoCliente getTipoCliente() {
        return tipoCliente;
    }
}

