package com.itau.seguros.solicitacao.domain.model;

/**
 * Enumeração que representa as categorias de seguro disponíveis.
 * 
 * Cada categoria possui regras específicas de validação de capital segurado
 * baseadas no tipo de cliente.
 */
public enum CategoriaSeguro {
    
    /**
     * Seguro de vida - proteção em caso de morte ou invalidez.
     */
    VIDA("Vida"),
    
    /**
     * Seguro automotivo - proteção para veículos.
     */
    AUTO("Auto"),
    
    /**
     * Seguro residencial - proteção para imóveis residenciais.
     */
    RESIDENCIAL("Residencial"),
    
    /**
     * Seguro empresarial - proteção para empresas e negócios.
     */
    EMPRESARIAL("Empresarial"),
    
    /**
     * Outros tipos de seguro não especificados acima.
     */
    OUTROS("Outros");
    
    private final String descricao;
    
    CategoriaSeguro(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
}

