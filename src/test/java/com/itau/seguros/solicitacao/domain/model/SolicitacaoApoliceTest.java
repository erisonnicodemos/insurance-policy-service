package com.itau.seguros.solicitacao.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a entidade SolicitacaoApolice.
 * 
 * Verifica o comportamento da entidade, incluindo transições de estado,
 * adição de histórico e validações de regras de negócio.
 */
public class SolicitacaoApoliceTest {

    @Test
    public void testCriacaoSolicitacao() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        String productId = "1b2da7cc-b367-4196-8a78-9cfeec21f587";
        CategoriaSeguro category = CategoriaSeguro.AUTO;
        String salesChannel = "MOBILE";
        String paymentMethod = "CREDIT_CARD";
        BigDecimal totalMonthlyPremiumAmount = new BigDecimal("75.25");
        BigDecimal insuredAmount = new BigDecimal("275000.50");
        
        Map<String, BigDecimal> coverages = new HashMap<>();
        coverages.put("Roubo", new BigDecimal("100000.25"));
        coverages.put("Perda Total", new BigDecimal("100000.25"));
        coverages.put("Colisão com Terceiros", new BigDecimal("75000.00"));
        
        var assistances = Arrays.asList("Guincho até 250km", "Troca de Óleo", "Chaveiro 24h");
        
        // Act
        SolicitacaoApolice solicitacao = new SolicitacaoApolice(
            customerId, productId, category, salesChannel, paymentMethod,
            totalMonthlyPremiumAmount, insuredAmount, coverages, assistances
        );
        
        // Assert
        assertNotNull(solicitacao);
        assertEquals(customerId, solicitacao.getCustomerId());
        assertEquals(productId, solicitacao.getProductId());
        assertEquals(category, solicitacao.getCategory());
        assertEquals(salesChannel, solicitacao.getSalesChannel());
        assertEquals(paymentMethod, solicitacao.getPaymentMethod());
        assertEquals(totalMonthlyPremiumAmount, solicitacao.getTotalMonthlyPremiumAmount());
        assertEquals(insuredAmount, solicitacao.getInsuredAmount());
        assertEquals(coverages.size(), solicitacao.getCoverages().size());
        assertEquals(assistances.size(), solicitacao.getAssistances().size());
        assertEquals(StatusSolicitacao.RECEBIDO, solicitacao.getStatus());
        assertNotNull(solicitacao.getCreatedAt());
        assertNull(solicitacao.getFinishedAt());
        assertEquals(1, solicitacao.getHistory().size());
        assertEquals(StatusSolicitacao.RECEBIDO, solicitacao.getHistory().get(0).getStatus());
    }
    
    @Test
    public void testAlterarStatus() {
        // Arrange
        SolicitacaoApolice solicitacao = criarSolicitacaoTeste();
        
        // Act
        solicitacao.alterarStatus(StatusSolicitacao.VALIDADO, "Validação aprovada");
        
        // Assert
        assertEquals(StatusSolicitacao.VALIDADO, solicitacao.getStatus());
        assertEquals(2, solicitacao.getHistory().size());
        assertEquals(StatusSolicitacao.VALIDADO, solicitacao.getHistory().get(1).getStatus());
        assertEquals("Validação aprovada", solicitacao.getHistory().get(1).getObservacao());
        assertNull(solicitacao.getFinishedAt());
    }
    
    @Test
    public void testAlterarStatusParaEstadoFinal() {
        // Arrange
        SolicitacaoApolice solicitacao = criarSolicitacaoTeste();
        solicitacao.alterarStatus(StatusSolicitacao.VALIDADO, "Validação aprovada");
        
        // Act
        solicitacao.alterarStatus(StatusSolicitacao.REJEITADA, "Capital segurado excede limite");
        
        // Assert
        assertEquals(StatusSolicitacao.REJEITADA, solicitacao.getStatus());
        assertEquals(3, solicitacao.getHistory().size());
        assertEquals(StatusSolicitacao.REJEITADA, solicitacao.getHistory().get(2).getStatus());
        assertEquals("Capital segurado excede limite", solicitacao.getHistory().get(2).getObservacao());
        assertNotNull(solicitacao.getFinishedAt());
    }
    
    @Test
    public void testAlterarStatusInvalido() {
        // Arrange
        SolicitacaoApolice solicitacao = criarSolicitacaoTeste();
        
        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            solicitacao.alterarStatus(StatusSolicitacao.APROVADA, "Não deveria permitir esta transição");
        });
        
        assertEquals(StatusSolicitacao.RECEBIDO, solicitacao.getStatus());
        assertEquals(1, solicitacao.getHistory().size());
    }
    
    @Test
    public void testPodeCancelar() {
        // Arrange
        SolicitacaoApolice solicitacao = criarSolicitacaoTeste();
        
        // Assert - Estado inicial RECEBIDO pode ser cancelado
        assertTrue(solicitacao.podeCancelar());
        
        // Act - Altera para VALIDADO
        solicitacao.alterarStatus(StatusSolicitacao.VALIDADO, "Validação aprovada");
        
        // Assert - VALIDADO pode ser cancelado
        assertTrue(solicitacao.podeCancelar());
        
        // Act - Altera para PENDENTE
        solicitacao.alterarStatus(StatusSolicitacao.PENDENTE, "Aguardando pagamento");
        
        // Assert - PENDENTE pode ser cancelado
        assertTrue(solicitacao.podeCancelar());
        
        // Act - Altera para APROVADA
        solicitacao.alterarStatus(StatusSolicitacao.APROVADA, "Pagamento confirmado");
        
        // Assert - APROVADA não pode ser cancelado
        assertFalse(solicitacao.podeCancelar());
    }
    
    @Test
    public void testEstadosFinaisNaoPodeCancelar() {
        // Arrange
        SolicitacaoApolice solicitacao = criarSolicitacaoTeste();
        solicitacao.alterarStatus(StatusSolicitacao.VALIDADO, "Validação aprovada");
        
        // Act - Altera para REJEITADA (estado final)
        solicitacao.alterarStatus(StatusSolicitacao.REJEITADA, "Rejeitada por fraude");
        
        // Assert - REJEITADA não pode ser cancelado
        assertFalse(solicitacao.podeCancelar());
        
        // Arrange - Nova solicitação
        solicitacao = criarSolicitacaoTeste();
        
        // Act - Altera para CANCELADA (estado final)
        solicitacao.alterarStatus(StatusSolicitacao.CANCELADA, "Cancelada pelo cliente");
        
        // Assert - CANCELADA não pode ser cancelado novamente
        assertFalse(solicitacao.podeCancelar());
    }
    
    /**
     * Método auxiliar para criar uma solicitação de teste.
     */
    private SolicitacaoApolice criarSolicitacaoTeste() {
        UUID customerId = UUID.randomUUID();
        String productId = "1b2da7cc-b367-4196-8a78-9cfeec21f587";
        CategoriaSeguro category = CategoriaSeguro.AUTO;
        String salesChannel = "MOBILE";
        String paymentMethod = "CREDIT_CARD";
        BigDecimal totalMonthlyPremiumAmount = new BigDecimal("75.25");
        BigDecimal insuredAmount = new BigDecimal("275000.50");
        
        Map<String, BigDecimal> coverages = new HashMap<>();
        coverages.put("Roubo", new BigDecimal("100000.25"));
        coverages.put("Perda Total", new BigDecimal("100000.25"));
        coverages.put("Colisão com Terceiros", new BigDecimal("75000.00"));
        
        var assistances = Arrays.asList("Guincho até 250km", "Troca de Óleo", "Chaveiro 24h");
        
        return new SolicitacaoApolice(
            customerId, productId, category, salesChannel, paymentMethod,
            totalMonthlyPremiumAmount, insuredAmount, coverages, assistances
        );
    }
}

