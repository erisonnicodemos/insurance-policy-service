package com.itau.seguros.solicitacao.api.dto;

import com.itau.seguros.solicitacao.domain.model.CategoriaSeguro;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO para recebimento de dados de uma nova solicitação de apólice.
 * 
 * Contém todas as informações necessárias para criar uma solicitação,
 * com validações básicas para garantir a integridade dos dados.
 */
public record SolicitacaoRequestDTO(
    
    @NotNull(message = "ID do cliente é obrigatório")
    UUID customerId,
    
    @NotBlank(message = "ID do produto é obrigatório")
    String productId,
    
    @NotNull(message = "Categoria do seguro é obrigatória")
    CategoriaSeguro category,
    
    @NotBlank(message = "Canal de vendas é obrigatório")
    String salesChannel,
    
    @NotBlank(message = "Forma de pagamento é obrigatória")
    String paymentMethod,
    
    @NotNull(message = "Valor do prêmio mensal é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor do prêmio mensal deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "Valor do prêmio mensal deve ter no máximo 8 dígitos inteiros e 2 decimais")
    BigDecimal totalMonthlyPremiumAmount,
    
    @NotNull(message = "Valor do capital segurado é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor do capital segurado deve ser maior que zero")
    @Digits(integer = 10, fraction = 2, message = "Valor do capital segurado deve ter no máximo 10 dígitos inteiros e 2 decimais")
    BigDecimal insuredAmount,
    
    @NotNull(message = "Coberturas são obrigatórias")
    @Size(min = 1, message = "Deve haver pelo menos uma cobertura")
    Map<String, BigDecimal> coverages,
    
    @NotNull(message = "Lista de assistências é obrigatória")
    List<String> assistances
) {
    
    /**
     * Valida se a soma das coberturas não excede o capital segurado.
     * 
     * @return true se a validação passou
     */
    public boolean isCoberturaValida() {
        if (coverages == null || coverages.isEmpty()) {
            return false;
        }
        
        BigDecimal somaCoberturas = coverages.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        return somaCoberturas.compareTo(insuredAmount) <= 0;
    }
    
    /**
     * Valida se todos os valores de cobertura são positivos.
     * 
     * @return true se todos os valores são válidos
     */
    public boolean isValoresCoberturaValidos() {
        if (coverages == null) {
            return false;
        }
        
        return coverages.values().stream()
            .allMatch(valor -> valor != null && valor.compareTo(BigDecimal.ZERO) > 0);
    }
}

