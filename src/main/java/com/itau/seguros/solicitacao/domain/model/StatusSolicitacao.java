package com.itau.seguros.solicitacao.domain.model;

/**
 * Enumeração que representa os possíveis estados do ciclo de vida 
 * de uma solicitação de apólice de seguro.
 * 
 * Estados e transições permitidas:
 * - RECEBIDO → VALIDADO, CANCELADA
 * - VALIDADO → PENDENTE, REJEITADA, CANCELADA
 * - PENDENTE → APROVADA, REJEITADA, CANCELADA
 * - REJEITADA → (estado final)
 * - APROVADA → (estado final)
 * - CANCELADA → (estado final)
 */
public enum StatusSolicitacao {
    
    /**
     * Estado inicial quando uma solicitação é criada.
     * Aguarda análise pela API de fraudes.
     */
    RECEBIDO("Recebido"),
    
    /**
     * Solicitação passou pela validação da API de fraudes
     * e atende às regras de negócio.
     */
    VALIDADO("Validado"),
    
    /**
     * Solicitação validada aguardando confirmação de pagamento
     * e autorização de subscrição.
     */
    PENDENTE("Pendente"),
    
    /**
     * Solicitação rejeitada por não atender às regras de validação,
     * falha no pagamento ou negativa na subscrição.
     */
    REJEITADA("Rejeitada"),
    
    /**
     * Solicitação aprovada após confirmação de pagamento
     * e autorização de subscrição. Estado final.
     */
    APROVADA("Aprovada"),
    
    /**
     * Solicitação cancelada a pedido do cliente.
     * Não pode ser cancelada se já aprovada.
     */
    CANCELADA("Cancelada");
    
    private final String descricao;
    
    StatusSolicitacao(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    /**
     * Verifica se é possível transicionar do estado atual para o novo estado.
     * 
     * @param novoStatus o status de destino
     * @return true se a transição é válida
     */
    public boolean podeTransicionarPara(StatusSolicitacao novoStatus) {
        return switch (this) {
            case RECEBIDO -> novoStatus == VALIDADO || novoStatus == CANCELADA;
            case VALIDADO -> novoStatus == PENDENTE || novoStatus == REJEITADA || novoStatus == CANCELADA;
            case PENDENTE -> novoStatus == APROVADA || novoStatus == REJEITADA || novoStatus == CANCELADA;
            case REJEITADA, APROVADA, CANCELADA -> false; // Estados finais
        };
    }
    
    /**
     * Verifica se o status atual é um estado final.
     * 
     * @return true se é um estado final
     */
    public boolean isEstadoFinal() {
        return this == REJEITADA || this == APROVADA || this == CANCELADA;
    }
}

