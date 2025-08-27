package com.itau.seguros.solicitacao.domain.event;

import com.itau.seguros.solicitacao.domain.model.CategoriaSeguro;
import com.itau.seguros.solicitacao.domain.model.StatusSolicitacao;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Classe base para eventos relacionados a solicitações de apólice.
 * 
 * Contém os dados comuns a todos os eventos de solicitação,
 * permitindo extensão para tipos específicos de eventos.
 */
public abstract class SolicitacaoEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final UUID id;
    private final UUID solicitacaoId;
    private final UUID customerId;
    private final String productId;
    private final CategoriaSeguro category;
    private final StatusSolicitacao status;
    private final BigDecimal insuredAmount;
    private final LocalDateTime timestamp;
    
    /**
     * Construtor para eventos de solicitação.
     * 
     * @param solicitacaoId ID da solicitação
     * @param customerId ID do cliente
     * @param productId ID do produto
     * @param category categoria do seguro
     * @param status status atual da solicitação
     * @param insuredAmount valor do capital segurado
     */
    protected SolicitacaoEvent(UUID solicitacaoId, UUID customerId, String productId,
                             CategoriaSeguro category, StatusSolicitacao status,
                             BigDecimal insuredAmount) {
        this.id = UUID.randomUUID();
        this.solicitacaoId = solicitacaoId;
        this.customerId = customerId;
        this.productId = productId;
        this.category = category;
        this.status = status;
        this.insuredAmount = insuredAmount;
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Retorna o tipo do evento para roteamento.
     * 
     * @return tipo do evento
     */
    public abstract String getEventType();
    
    // Getters
    
    public UUID getId() {
        return id;
    }
    
    public UUID getSolicitacaoId() {
        return solicitacaoId;
    }
    
    public UUID getCustomerId() {
        return customerId;
    }
    
    public String getProductId() {
        return productId;
    }
    
    public CategoriaSeguro getCategory() {
        return category;
    }
    
    public StatusSolicitacao getStatus() {
        return status;
    }
    
    public BigDecimal getInsuredAmount() {
        return insuredAmount;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return "SolicitacaoEvent{" +
                "id=" + id +
                ", solicitacaoId=" + solicitacaoId +
                ", customerId=" + customerId +
                ", productId='" + productId + '\'' +
                ", category=" + category +
                ", status=" + status +
                ", timestamp=" + timestamp +
                ", eventType='" + getEventType() + '\'' +
                '}';
    }
}

