package com.itau.seguros.solicitacao.domain.rule;

import com.itau.seguros.solicitacao.domain.model.CategoriaSeguro;
import com.itau.seguros.solicitacao.domain.model.TipoCliente;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Classe responsável por aplicar as regras de validação de capital segurado
 * baseadas no tipo de cliente e categoria do seguro.
 * 
 * Implementa as regras de negócio definidas no case para determinar
 * se uma solicitação deve ser aprovada ou rejeitada com base no
 * perfil de risco do cliente.
 */
@Component
public class RegraValidacaoCliente {
    
    /**
     * Valida se o capital segurado está dentro dos limites permitidos
     * para o tipo de cliente e categoria de seguro.
     * 
     * @param tipoCliente classificação de risco do cliente
     * @param categoria categoria do seguro
     * @param capitalSegurado valor do capital segurado
     * @return true se o capital está dentro dos limites
     */
    public boolean validarCapitalSegurado(TipoCliente tipoCliente, CategoriaSeguro categoria, BigDecimal capitalSegurado) {
        BigDecimal limite = obterLimiteCapitalSegurado(tipoCliente, categoria);
        return capitalSegurado.compareTo(limite) <= 0;
    }
    
    /**
     * Obtém o limite máximo de capital segurado para um tipo de cliente
     * e categoria de seguro específicos.
     * 
     * @param tipoCliente classificação de risco do cliente
     * @param categoria categoria do seguro
     * @return limite máximo permitido
     */
    public BigDecimal obterLimiteCapitalSegurado(TipoCliente tipoCliente, CategoriaSeguro categoria) {
        return switch (tipoCliente) {
            case REGULAR -> obterLimiteClienteRegular(categoria);
            case ALTO_RISCO -> obterLimiteClienteAltoRisco(categoria);
            case PREFERENCIAL -> obterLimiteClientePreferencial(categoria);
            case SEM_INFORMACAO -> obterLimiteClienteSemInformacao(categoria);
        };
    }
    
    /**
     * Limites para cliente regular.
     */
    private BigDecimal obterLimiteClienteRegular(CategoriaSeguro categoria) {
        return switch (categoria) {
            case VIDA, RESIDENCIAL -> new BigDecimal("500000.00");
            case AUTO -> new BigDecimal("350000.00");
            case EMPRESARIAL, OUTROS -> new BigDecimal("255000.00");
        };
    }
    
    /**
     * Limites para cliente alto risco.
     */
    private BigDecimal obterLimiteClienteAltoRisco(CategoriaSeguro categoria) {
        return switch (categoria) {
            case AUTO -> new BigDecimal("250000.00");
            case RESIDENCIAL -> new BigDecimal("150000.00");
            case VIDA, EMPRESARIAL, OUTROS -> new BigDecimal("125000.00");
        };
    }
    
    /**
     * Limites para cliente preferencial.
     */
    private BigDecimal obterLimiteClientePreferencial(CategoriaSeguro categoria) {
        return switch (categoria) {
            case VIDA -> new BigDecimal("800000.00");
            case AUTO, RESIDENCIAL -> new BigDecimal("450000.00");
            case EMPRESARIAL, OUTROS -> new BigDecimal("375000.00");
        };
    }
    
    /**
     * Limites para cliente sem informação.
     */
    private BigDecimal obterLimiteClienteSemInformacao(CategoriaSeguro categoria) {
        return switch (categoria) {
            case VIDA, RESIDENCIAL -> new BigDecimal("200000.00");
            case AUTO -> new BigDecimal("75000.00");
            case EMPRESARIAL, OUTROS -> new BigDecimal("55000.00");
        };
    }
    
    /**
     * Retorna uma mensagem explicativa sobre o motivo da rejeição.
     * 
     * @param tipoCliente classificação de risco do cliente
     * @param categoria categoria do seguro
     * @param capitalSegurado valor solicitado
     * @return mensagem explicativa
     */
    public String obterMensagemRejeicao(TipoCliente tipoCliente, CategoriaSeguro categoria, BigDecimal capitalSegurado) {
        BigDecimal limite = obterLimiteCapitalSegurado(tipoCliente, categoria);
        return String.format(
            "Capital segurado de R$ %s excede o limite de R$ %s para cliente %s em seguro %s",
            capitalSegurado, limite, tipoCliente.getDescricao(), categoria.getDescricao()
        );
    }
}

