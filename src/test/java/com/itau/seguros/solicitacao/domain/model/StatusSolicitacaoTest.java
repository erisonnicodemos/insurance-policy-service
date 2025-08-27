package com.itau.seguros.solicitacao.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a enumeração StatusSolicitacao.
 * 
 * Verifica as regras de transição de estados e outras funcionalidades
 * da enumeração.
 */
public class StatusSolicitacaoTest {

    @Test
    public void testTransicoesPermitidas() {
        // RECEBIDO pode transicionar para VALIDADO ou CANCELADA
        assertTrue(StatusSolicitacao.RECEBIDO.podeTransicionarPara(StatusSolicitacao.VALIDADO));
        assertTrue(StatusSolicitacao.RECEBIDO.podeTransicionarPara(StatusSolicitacao.CANCELADA));
        assertFalse(StatusSolicitacao.RECEBIDO.podeTransicionarPara(StatusSolicitacao.PENDENTE));
        assertFalse(StatusSolicitacao.RECEBIDO.podeTransicionarPara(StatusSolicitacao.REJEITADA));
        assertFalse(StatusSolicitacao.RECEBIDO.podeTransicionarPara(StatusSolicitacao.APROVADA));
        
        // VALIDADO pode transicionar para PENDENTE, REJEITADA ou CANCELADA
        assertTrue(StatusSolicitacao.VALIDADO.podeTransicionarPara(StatusSolicitacao.PENDENTE));
        assertTrue(StatusSolicitacao.VALIDADO.podeTransicionarPara(StatusSolicitacao.REJEITADA));
        assertTrue(StatusSolicitacao.VALIDADO.podeTransicionarPara(StatusSolicitacao.CANCELADA));
        assertFalse(StatusSolicitacao.VALIDADO.podeTransicionarPara(StatusSolicitacao.RECEBIDO));
        assertFalse(StatusSolicitacao.VALIDADO.podeTransicionarPara(StatusSolicitacao.APROVADA));
        
        // PENDENTE pode transicionar para APROVADA, REJEITADA ou CANCELADA
        assertTrue(StatusSolicitacao.PENDENTE.podeTransicionarPara(StatusSolicitacao.APROVADA));
        assertTrue(StatusSolicitacao.PENDENTE.podeTransicionarPara(StatusSolicitacao.REJEITADA));
        assertTrue(StatusSolicitacao.PENDENTE.podeTransicionarPara(StatusSolicitacao.CANCELADA));
        assertFalse(StatusSolicitacao.PENDENTE.podeTransicionarPara(StatusSolicitacao.RECEBIDO));
        assertFalse(StatusSolicitacao.PENDENTE.podeTransicionarPara(StatusSolicitacao.VALIDADO));
        
        // Estados finais não podem transicionar para nenhum outro estado
        for (StatusSolicitacao destino : StatusSolicitacao.values()) {
            assertFalse(StatusSolicitacao.REJEITADA.podeTransicionarPara(destino));
            assertFalse(StatusSolicitacao.APROVADA.podeTransicionarPara(destino));
            assertFalse(StatusSolicitacao.CANCELADA.podeTransicionarPara(destino));
        }
    }
    
    @Test
    public void testEstadosFinais() {
        assertTrue(StatusSolicitacao.REJEITADA.isEstadoFinal());
        assertTrue(StatusSolicitacao.APROVADA.isEstadoFinal());
        assertTrue(StatusSolicitacao.CANCELADA.isEstadoFinal());
        
        assertFalse(StatusSolicitacao.RECEBIDO.isEstadoFinal());
        assertFalse(StatusSolicitacao.VALIDADO.isEstadoFinal());
        assertFalse(StatusSolicitacao.PENDENTE.isEstadoFinal());
    }
    
    @Test
    public void testDescricao() {
        assertEquals("Recebido", StatusSolicitacao.RECEBIDO.getDescricao());
        assertEquals("Validado", StatusSolicitacao.VALIDADO.getDescricao());
        assertEquals("Pendente", StatusSolicitacao.PENDENTE.getDescricao());
        assertEquals("Rejeitada", StatusSolicitacao.REJEITADA.getDescricao());
        assertEquals("Aprovada", StatusSolicitacao.APROVADA.getDescricao());
        assertEquals("Cancelada", StatusSolicitacao.CANCELADA.getDescricao());
    }
}

