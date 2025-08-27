package com.itau.seguros.solicitacao.domain.model;

/**
 * Enumeração que representa os tipos de classificação de risco
 * de clientes retornados pela API de Fraudes.
 * 
 * Cada tipo possui regras específicas de validação baseadas
 * no capital segurado e categoria do seguro.
 */
public enum TipoCliente {
    
    /**
     * Cliente com perfil de risco baixo e histórico comum.
     * Limites padrão para aprovação.
     */
    REGULAR("Regular"),
    
    /**
     * Cliente com perfil de maior risco devido a comportamento
     * ou histórico de problemas com sinistros.
     * Limites mais restritivos.
     */
    ALTO_RISCO("Alto Risco"),
    
    /**
     * Cliente com bom relacionamento com a seguradora.
     * Limites mais altos para aprovação.
     */
    PREFERENCIAL("Preferencial"),
    
    /**
     * Cliente sem histórico ou com pouco histórico.
     * Análise mais cautelosa com limites baixos.
     */
    SEM_INFORMACAO("Sem Informação");
    
    private final String descricao;
    
    TipoCliente(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
}

