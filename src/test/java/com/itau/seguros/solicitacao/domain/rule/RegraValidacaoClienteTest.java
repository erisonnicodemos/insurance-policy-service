package com.itau.seguros.solicitacao.domain.rule;

import com.itau.seguros.solicitacao.domain.model.CategoriaSeguro;
import com.itau.seguros.solicitacao.domain.model.TipoCliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe RegraValidacaoCliente.
 * 
 * Verifica as regras de validação de capital segurado para
 * diferentes tipos de cliente e categorias de seguro.
 */
public class RegraValidacaoClienteTest {

    private RegraValidacaoCliente regraValidacao;
    
    @BeforeEach
    public void setup() {
        regraValidacao = new RegraValidacaoCliente();
    }
    
    @Test
    public void testValidarCapitalSeguradoClienteRegular() {
        // Dentro do limite
        assertTrue(regraValidacao.validarCapitalSegurado(
            TipoCliente.REGULAR, 
            CategoriaSeguro.VIDA, 
            new BigDecimal("500000.00")
        ));
        
        // No limite
        assertTrue(regraValidacao.validarCapitalSegurado(
            TipoCliente.REGULAR, 
            CategoriaSeguro.AUTO, 
            new BigDecimal("350000.00")
        ));
        
        // Acima do limite
        assertFalse(regraValidacao.validarCapitalSegurado(
            TipoCliente.REGULAR, 
            CategoriaSeguro.VIDA, 
            new BigDecimal("500000.01")
        ));
        
        // Acima do limite
        assertFalse(regraValidacao.validarCapitalSegurado(
            TipoCliente.REGULAR, 
            CategoriaSeguro.AUTO, 
            new BigDecimal("350000.01")
        ));
    }
    
    @Test
    public void testValidarCapitalSeguradoClienteAltoRisco() {
        // Dentro do limite
        assertTrue(regraValidacao.validarCapitalSegurado(
            TipoCliente.ALTO_RISCO, 
            CategoriaSeguro.AUTO, 
            new BigDecimal("250000.00")
        ));
        
        // No limite
        assertTrue(regraValidacao.validarCapitalSegurado(
            TipoCliente.ALTO_RISCO, 
            CategoriaSeguro.RESIDENCIAL, 
            new BigDecimal("150000.00")
        ));
        
        // Acima do limite
        assertFalse(regraValidacao.validarCapitalSegurado(
            TipoCliente.ALTO_RISCO, 
            CategoriaSeguro.AUTO, 
            new BigDecimal("250000.01")
        ));
    }
    
    @Test
    public void testValidarCapitalSeguradoClientePreferencial() {
        // Dentro do limite
        assertTrue(regraValidacao.validarCapitalSegurado(
            TipoCliente.PREFERENCIAL, 
            CategoriaSeguro.VIDA, 
            new BigDecimal("799999.99")
        ));
        
        // No limite
        assertTrue(regraValidacao.validarCapitalSegurado(
            TipoCliente.PREFERENCIAL, 
            CategoriaSeguro.AUTO, 
            new BigDecimal("450000.00")
        ));
        
        // Acima do limite
        assertFalse(regraValidacao.validarCapitalSegurado(
            TipoCliente.PREFERENCIAL, 
            CategoriaSeguro.VIDA, 
            new BigDecimal("800000.01")
        ));
    }
    
    @Test
    public void testValidarCapitalSeguradoClienteSemInformacao() {
        // Dentro do limite
        assertTrue(regraValidacao.validarCapitalSegurado(
            TipoCliente.SEM_INFORMACAO, 
            CategoriaSeguro.VIDA, 
            new BigDecimal("200000.00")
        ));
        
        // No limite
        assertTrue(regraValidacao.validarCapitalSegurado(
            TipoCliente.SEM_INFORMACAO, 
            CategoriaSeguro.AUTO, 
            new BigDecimal("75000.00")
        ));
        
        // Acima do limite
        assertFalse(regraValidacao.validarCapitalSegurado(
            TipoCliente.SEM_INFORMACAO, 
            CategoriaSeguro.AUTO, 
            new BigDecimal("75000.01")
        ));
    }
    
    @Test
    public void testObterMensagemRejeicao() {
        BigDecimal capital = new BigDecimal("500000.01");
        String mensagem = regraValidacao.obterMensagemRejeicao(
            TipoCliente.REGULAR, 
            CategoriaSeguro.VIDA, 
            capital
        );
        
        assertTrue(mensagem.contains("500000.01"));
        assertTrue(mensagem.contains("500000.00"));
        assertTrue(mensagem.contains("Regular"));
        assertTrue(mensagem.contains("Vida"));
    }
    
    /**
     * Teste parametrizado para verificar limites de capital segurado
     * para diferentes combinações de tipo de cliente e categoria.
     */
    @ParameterizedTest
    @MethodSource("providerLimitesCapitalSegurado")
    public void testLimitesCapitalSegurado(TipoCliente tipoCliente, CategoriaSeguro categoria, 
                                          BigDecimal valorLimite, boolean deveAprovar) {
        assertEquals(deveAprovar, regraValidacao.validarCapitalSegurado(tipoCliente, categoria, valorLimite));
    }
    
    /**
     * Provedor de dados para o teste parametrizado.
     */
    private static Stream<Arguments> providerLimitesCapitalSegurado() {
        return Stream.of(
            // Cliente Regular
            Arguments.of(TipoCliente.REGULAR, CategoriaSeguro.VIDA, new BigDecimal("500000.00"), true),
            Arguments.of(TipoCliente.REGULAR, CategoriaSeguro.VIDA, new BigDecimal("500000.01"), false),
            Arguments.of(TipoCliente.REGULAR, CategoriaSeguro.AUTO, new BigDecimal("350000.00"), true),
            Arguments.of(TipoCliente.REGULAR, CategoriaSeguro.AUTO, new BigDecimal("350000.01"), false),
            Arguments.of(TipoCliente.REGULAR, CategoriaSeguro.OUTROS, new BigDecimal("255000.00"), true),
            Arguments.of(TipoCliente.REGULAR, CategoriaSeguro.OUTROS, new BigDecimal("255000.01"), false),
            
            // Cliente Alto Risco
            Arguments.of(TipoCliente.ALTO_RISCO, CategoriaSeguro.AUTO, new BigDecimal("250000.00"), true),
            Arguments.of(TipoCliente.ALTO_RISCO, CategoriaSeguro.AUTO, new BigDecimal("250000.01"), false),
            Arguments.of(TipoCliente.ALTO_RISCO, CategoriaSeguro.RESIDENCIAL, new BigDecimal("150000.00"), true),
            Arguments.of(TipoCliente.ALTO_RISCO, CategoriaSeguro.RESIDENCIAL, new BigDecimal("150000.01"), false),
            Arguments.of(TipoCliente.ALTO_RISCO, CategoriaSeguro.VIDA, new BigDecimal("125000.00"), true),
            Arguments.of(TipoCliente.ALTO_RISCO, CategoriaSeguro.VIDA, new BigDecimal("125000.01"), false),
            
            // Cliente Preferencial
            Arguments.of(TipoCliente.PREFERENCIAL, CategoriaSeguro.VIDA, new BigDecimal("800000.00"), true),
            Arguments.of(TipoCliente.PREFERENCIAL, CategoriaSeguro.VIDA, new BigDecimal("800000.01"), false),
            Arguments.of(TipoCliente.PREFERENCIAL, CategoriaSeguro.AUTO, new BigDecimal("450000.00"), true),
            Arguments.of(TipoCliente.PREFERENCIAL, CategoriaSeguro.AUTO, new BigDecimal("450000.01"), false),
            Arguments.of(TipoCliente.PREFERENCIAL, CategoriaSeguro.OUTROS, new BigDecimal("375000.00"), true),
            Arguments.of(TipoCliente.PREFERENCIAL, CategoriaSeguro.OUTROS, new BigDecimal("375000.01"), false),
            
            // Cliente Sem Informação
            Arguments.of(TipoCliente.SEM_INFORMACAO, CategoriaSeguro.VIDA, new BigDecimal("200000.00"), true),
            Arguments.of(TipoCliente.SEM_INFORMACAO, CategoriaSeguro.VIDA, new BigDecimal("200000.01"), false),
            Arguments.of(TipoCliente.SEM_INFORMACAO, CategoriaSeguro.AUTO, new BigDecimal("75000.00"), true),
            Arguments.of(TipoCliente.SEM_INFORMACAO, CategoriaSeguro.AUTO, new BigDecimal("75000.01"), false),
            Arguments.of(TipoCliente.SEM_INFORMACAO, CategoriaSeguro.OUTROS, new BigDecimal("55000.00"), true),
            Arguments.of(TipoCliente.SEM_INFORMACAO, CategoriaSeguro.OUTROS, new BigDecimal("55000.01"), false)
        );
    }
}

