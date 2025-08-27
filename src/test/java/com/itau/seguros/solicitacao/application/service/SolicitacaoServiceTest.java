package com.itau.seguros.solicitacao.application.service;

import com.itau.seguros.solicitacao.api.dto.FraudeResponseDTO;
import com.itau.seguros.solicitacao.api.dto.SolicitacaoRequestDTO;
import com.itau.seguros.solicitacao.api.dto.SolicitacaoResponseDTO;
import com.itau.seguros.solicitacao.domain.event.SolicitacaoRecebidaEvent;
import com.itau.seguros.solicitacao.domain.model.CategoriaSeguro;
import com.itau.seguros.solicitacao.domain.model.SolicitacaoApolice;
import com.itau.seguros.solicitacao.domain.model.StatusSolicitacao;
import com.itau.seguros.solicitacao.domain.model.TipoCliente;
import com.itau.seguros.solicitacao.domain.repository.SolicitacaoRepository;
import com.itau.seguros.solicitacao.domain.rule.RegraValidacaoCliente;
import com.itau.seguros.solicitacao.infrastructure.external.ApiFraudesClient;
import com.itau.seguros.solicitacao.infrastructure.messaging.SolicitacaoEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para o serviço SolicitacaoService.
 * 
 * Verifica o comportamento do serviço, incluindo criação de solicitações,
 * processamento de validação e interação com dependências.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SolicitacaoServiceTest {

    @Mock
    private SolicitacaoRepository repository;
    
    @Mock
    private ApiFraudesClient apiFraudesClient;
    
    @Mock
    private RegraValidacaoCliente regraValidacao;
    
    @Mock
    private SolicitacaoEventProducer eventProducer;
    
    @Captor
    private ArgumentCaptor<SolicitacaoApolice> solicitacaoCaptor;
    
    @Captor
    private ArgumentCaptor<SolicitacaoRecebidaEvent> eventCaptor;
    
    private SolicitacaoService service;
    
    @BeforeEach
    public void setup() {
        service = new SolicitacaoService(repository, apiFraudesClient, regraValidacao, eventProducer);
    }
    
    @Test
    public void testCriarSolicitacao() {
        // Arrange
        SolicitacaoRequestDTO request = criarRequestDTO();
        
        UUID solicitacaoId = UUID.randomUUID();
        SolicitacaoApolice solicitacaoSalva = mock(SolicitacaoApolice.class);
        when(solicitacaoSalva.getId()).thenReturn(solicitacaoId);
        when(solicitacaoSalva.getCustomerId()).thenReturn(request.customerId());
        when(solicitacaoSalva.getProductId()).thenReturn(request.productId());
        when(solicitacaoSalva.getCategory()).thenReturn(request.category());
        when(solicitacaoSalva.getInsuredAmount()).thenReturn(request.insuredAmount());
        when(solicitacaoSalva.getStatus()).thenReturn(StatusSolicitacao.RECEBIDO);
        when(solicitacaoSalva.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(solicitacaoSalva.getHistory()).thenReturn(Collections.emptyList());
        when(solicitacaoSalva.getCoverages()).thenReturn(request.coverages());
        when(solicitacaoSalva.getAssistances()).thenReturn(request.assistances());
        
        when(repository.save(any(SolicitacaoApolice.class))).thenReturn(solicitacaoSalva);
        
        // Act
        SolicitacaoResponseDTO response = service.criarSolicitacao(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(solicitacaoId, response.id());
        assertEquals(request.customerId(), response.customerId());
        assertEquals(request.productId(), response.productId());
        assertEquals(request.category(), response.category());
        assertEquals(StatusSolicitacao.RECEBIDO, response.status());
        
        verify(repository).save(solicitacaoCaptor.capture());
        SolicitacaoApolice solicitacaoCapturada = solicitacaoCaptor.getValue();
        assertEquals(request.customerId(), solicitacaoCapturada.getCustomerId());
        assertEquals(request.productId(), solicitacaoCapturada.getProductId());
        assertEquals(request.category(), solicitacaoCapturada.getCategory());
        
        verify(eventProducer).publicarEvento(eventCaptor.capture());
        SolicitacaoRecebidaEvent eventoCapturado = eventCaptor.getValue();
        assertEquals(solicitacaoId, eventoCapturado.getSolicitacaoId());
        assertEquals(request.customerId(), eventoCapturado.getCustomerId());
    }
    
    @Test
    public void testProcessarValidacaoAprovada() {
        // Arrange
        UUID solicitacaoId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        
        SolicitacaoApolice solicitacao = mock(SolicitacaoApolice.class);
        when(solicitacao.getId()).thenReturn(solicitacaoId);
        when(solicitacao.getCustomerId()).thenReturn(customerId);
        when(solicitacao.getProductId()).thenReturn("produto-123");
        when(solicitacao.getCategory()).thenReturn(CategoriaSeguro.AUTO);
        when(solicitacao.getInsuredAmount()).thenReturn(new BigDecimal("250000.00"));
        when(solicitacao.getStatus()).thenReturn(StatusSolicitacao.RECEBIDO);
        
        when(repository.findById(solicitacaoId)).thenReturn(Optional.of(solicitacao));
        
        FraudeResponseDTO fraudeResponse = new FraudeResponseDTO(
            solicitacaoId,
            customerId,
            LocalDateTime.now(),
            TipoCliente.REGULAR,
            Collections.emptyList()
        );
        
        when(apiFraudesClient.consultarClassificacaoRisco(solicitacaoId, customerId))
            .thenReturn(fraudeResponse);
        
        when(regraValidacao.validarCapitalSegurado(
            TipoCliente.REGULAR, 
            CategoriaSeguro.AUTO, 
            new BigDecimal("250000.00")
        )).thenReturn(true);
        
        // Act
        service.processarValidacao(solicitacaoId);
        
        // Assert
        verify(solicitacao).alterarStatus(StatusSolicitacao.VALIDADO, "Validação aprovada pela API de Fraudes");
        verify(repository).save(solicitacao);
        verify(eventProducer).publicarEvento(any());
    }
    
    @Test
    public void testProcessarValidacaoRejeitada() {
        // Arrange
        UUID solicitacaoId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        
        SolicitacaoApolice solicitacao = mock(SolicitacaoApolice.class);
        when(solicitacao.getId()).thenReturn(solicitacaoId);
        when(solicitacao.getCustomerId()).thenReturn(customerId);
        when(solicitacao.getProductId()).thenReturn("produto-123");
        when(solicitacao.getCategory()).thenReturn(CategoriaSeguro.AUTO);
        when(solicitacao.getInsuredAmount()).thenReturn(new BigDecimal("350000.01"));
        when(solicitacao.getStatus()).thenReturn(StatusSolicitacao.RECEBIDO);
        
        when(repository.findById(solicitacaoId)).thenReturn(Optional.of(solicitacao));
        
        FraudeResponseDTO fraudeResponse = new FraudeResponseDTO(
            solicitacaoId,
            customerId,
            LocalDateTime.now(),
            TipoCliente.REGULAR,
            Collections.emptyList()
        );
        
        when(apiFraudesClient.consultarClassificacaoRisco(solicitacaoId, customerId))
            .thenReturn(fraudeResponse);
        
        when(regraValidacao.validarCapitalSegurado(
            TipoCliente.REGULAR, 
            CategoriaSeguro.AUTO, 
            new BigDecimal("350000.01")
        )).thenReturn(false);
        
        String mensagemRejeicao = "Capital segurado excede limite";
        when(regraValidacao.obterMensagemRejeicao(
            TipoCliente.REGULAR, 
            CategoriaSeguro.AUTO, 
            new BigDecimal("350000.01")
        )).thenReturn(mensagemRejeicao);
        
        // Act
        service.processarValidacao(solicitacaoId);
        
        // Assert
        verify(solicitacao).alterarStatus(StatusSolicitacao.REJEITADA, mensagemRejeicao);
        verify(repository).save(solicitacao);
        verify(eventProducer).publicarEvento(any());
    }
    
    @Test
    public void testBuscarPorId() {
        // Arrange
        UUID solicitacaoId = UUID.randomUUID();
        SolicitacaoApolice solicitacao = mock(SolicitacaoApolice.class);
        when(solicitacao.getId()).thenReturn(solicitacaoId);
        when(solicitacao.getStatus()).thenReturn(StatusSolicitacao.VALIDADO);
        when(solicitacao.getHistory()).thenReturn(Collections.emptyList());
        
        when(repository.findByIdWithHistory(solicitacaoId)).thenReturn(Optional.of(solicitacao));
        
        // Act
        Optional<SolicitacaoResponseDTO> resultado = service.buscarPorId(solicitacaoId);
        
        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(solicitacaoId, resultado.get().id());
        assertEquals(StatusSolicitacao.VALIDADO, resultado.get().status());
    }
    
    @Test
    public void testCancelarSolicitacao() {
        // Arrange
        UUID solicitacaoId = UUID.randomUUID();
        SolicitacaoApolice solicitacao = mock(SolicitacaoApolice.class);
        when(solicitacao.getId()).thenReturn(solicitacaoId);
        when(solicitacao.getStatus()).thenReturn(StatusSolicitacao.VALIDADO);
        when(solicitacao.podeCancelar()).thenReturn(true);
        when(solicitacao.getHistory()).thenReturn(Collections.emptyList());
        
        when(repository.findById(solicitacaoId)).thenReturn(Optional.of(solicitacao));
        when(repository.save(any(SolicitacaoApolice.class))).thenReturn(solicitacao);
        
        // Act
        Optional<SolicitacaoResponseDTO> resultado = service.cancelarSolicitacao(solicitacaoId);
        
        // Assert
        assertTrue(resultado.isPresent());
        verify(solicitacao).alterarStatus(StatusSolicitacao.CANCELADA, "Cancelamento solicitado pelo cliente");
        verify(repository).save(solicitacao);
    }
    
    @Test
    public void testCancelarSolicitacaoNaoPermitido() {
        // Arrange
        UUID solicitacaoId = UUID.randomUUID();
        SolicitacaoApolice solicitacao = mock(SolicitacaoApolice.class);
        when(solicitacao.getId()).thenReturn(solicitacaoId);
        when(solicitacao.getStatus()).thenReturn(StatusSolicitacao.APROVADA);
        when(solicitacao.podeCancelar()).thenReturn(false);
        
        when(repository.findById(solicitacaoId)).thenReturn(Optional.of(solicitacao));
        
        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            service.cancelarSolicitacao(solicitacaoId);
        });
        
        verify(solicitacao, never()).alterarStatus(any(), any());
        verify(repository, never()).save(solicitacao);
    }
    
    /**
     * Método auxiliar para criar um DTO de requisição para testes.
     */
    private SolicitacaoRequestDTO criarRequestDTO() {
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
        
        List<String> assistances = Arrays.asList("Guincho até 250km", "Troca de Óleo", "Chaveiro 24h");
        
        return new SolicitacaoRequestDTO(
            customerId, productId, category, salesChannel, paymentMethod,
            totalMonthlyPremiumAmount, insuredAmount, coverages, assistances
        );
    }
}

