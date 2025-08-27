package com.itau.seguros.solicitacao.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itau.seguros.solicitacao.api.dto.SolicitacaoRequestDTO;
import com.itau.seguros.solicitacao.api.dto.SolicitacaoResponseDTO;
import com.itau.seguros.solicitacao.application.service.SolicitacaoService;
import com.itau.seguros.solicitacao.domain.model.CategoriaSeguro;
import com.itau.seguros.solicitacao.domain.model.StatusSolicitacao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para o controller SolicitacaoController.
 * 
 * Verifica o comportamento dos endpoints REST, incluindo validação de
 * requisições, respostas e tratamento de erros.
 */
@WebMvcTest(SolicitacaoController.class)
public class SolicitacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private SolicitacaoService service;
    
    @Test
    public void testCriarSolicitacao() throws Exception {
        // Arrange
        SolicitacaoRequestDTO request = criarRequestDTO();
        
        UUID solicitacaoId = UUID.randomUUID();
        SolicitacaoResponseDTO response = new SolicitacaoResponseDTO(
            solicitacaoId,
            request.customerId(),
            request.productId(),
            request.category(),
            request.salesChannel(),
            request.paymentMethod(),
            StatusSolicitacao.RECEBIDO,
            LocalDateTime.now(),
            null,
            request.totalMonthlyPremiumAmount(),
            request.insuredAmount(),
            request.coverages(),
            request.assistances(),
            Collections.emptyList()
        );
        
        when(service.criarSolicitacao(any(SolicitacaoRequestDTO.class))).thenReturn(response);
        
        // Act & Assert
        mockMvc.perform(post("/api/solicitacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(solicitacaoId.toString()))
            .andExpect(jsonPath("$.customerId").value(request.customerId().toString()))
            .andExpect(jsonPath("$.status").value("RECEBIDO"));
        
        verify(service).criarSolicitacao(any(SolicitacaoRequestDTO.class));
    }
    
    @Test
    public void testCriarSolicitacaoInvalida() throws Exception {
        // Arrange
        Map<String, Object> requestInvalida = new HashMap<>();
        requestInvalida.put("customerId", UUID.randomUUID().toString());
        // Faltando campos obrigatórios
        
        // Act & Assert
        mockMvc.perform(post("/api/solicitacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalida)))
            .andExpect(status().isBadRequest());
        
        verify(service, never()).criarSolicitacao(any());
    }
    
    @Test
    public void testBuscarPorId() throws Exception {
        // Arrange
        UUID solicitacaoId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        
        SolicitacaoResponseDTO response = new SolicitacaoResponseDTO(
            solicitacaoId,
            customerId,
            "produto-123",
            CategoriaSeguro.AUTO,
            "MOBILE",
            "CREDIT_CARD",
            StatusSolicitacao.VALIDADO,
            LocalDateTime.now(),
            null,
            new BigDecimal("75.25"),
            new BigDecimal("275000.50"),
            Map.of("Roubo", new BigDecimal("100000.25")),
            List.of("Guincho até 250km"),
            Collections.emptyList()
        );
        
        when(service.buscarPorId(solicitacaoId)).thenReturn(Optional.of(response));
        
        // Act & Assert
        mockMvc.perform(get("/api/solicitacoes/{id}", solicitacaoId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(solicitacaoId.toString()))
            .andExpect(jsonPath("$.customerId").value(customerId.toString()))
            .andExpect(jsonPath("$.status").value("VALIDADO"));
        
        verify(service).buscarPorId(solicitacaoId);
    }
    
    @Test
    public void testBuscarPorIdNaoEncontrado() throws Exception {
        // Arrange
        UUID solicitacaoId = UUID.randomUUID();
        
        when(service.buscarPorId(solicitacaoId)).thenReturn(Optional.empty());
        
        // Act & Assert
        mockMvc.perform(get("/api/solicitacoes/{id}", solicitacaoId))
            .andExpect(status().isNotFound());
        
        verify(service).buscarPorId(solicitacaoId);
    }
    
    @Test
    public void testBuscarPorCustomerId() throws Exception {
        // Arrange
        UUID customerId = UUID.randomUUID();
        
        List<SolicitacaoResponseDTO> solicitacoes = Arrays.asList(
            new SolicitacaoResponseDTO(
                UUID.randomUUID(),
                customerId,
                "produto-123",
                CategoriaSeguro.AUTO,
                "MOBILE",
                "CREDIT_CARD",
                StatusSolicitacao.VALIDADO,
                LocalDateTime.now(),
                null,
                new BigDecimal("75.25"),
                new BigDecimal("275000.50"),
                Map.of("Roubo", new BigDecimal("100000.25")),
                List.of("Guincho até 250km"),
                Collections.emptyList()
            ),
            new SolicitacaoResponseDTO(
                UUID.randomUUID(),
                customerId,
                "produto-456",
                CategoriaSeguro.VIDA,
                "WEB",
                "BOLETO",
                StatusSolicitacao.PENDENTE,
                LocalDateTime.now(),
                null,
                new BigDecimal("120.50"),
                new BigDecimal("500000.00"),
                Map.of("Morte", new BigDecimal("500000.00")),
                List.of("Assistência funeral"),
                Collections.emptyList()
            )
        );
        
        when(service.buscarPorCustomerId(customerId)).thenReturn(solicitacoes);
        
        // Act & Assert
        mockMvc.perform(get("/api/solicitacoes")
                .param("customerId", customerId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].customerId").value(customerId.toString()))
            .andExpect(jsonPath("$[1].customerId").value(customerId.toString()));
        
        verify(service).buscarPorCustomerId(customerId);
    }
    
    @Test
    public void testCancelarSolicitacao() throws Exception {
        // Arrange
        UUID solicitacaoId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        
        SolicitacaoResponseDTO response = new SolicitacaoResponseDTO(
            solicitacaoId,
            customerId,
            "produto-123",
            CategoriaSeguro.AUTO,
            "MOBILE",
            "CREDIT_CARD",
            StatusSolicitacao.CANCELADA,
            LocalDateTime.now(),
            LocalDateTime.now(),
            new BigDecimal("75.25"),
            new BigDecimal("275000.50"),
            Map.of("Roubo", new BigDecimal("100000.25")),
            List.of("Guincho até 250km"),
            Collections.emptyList()
        );
        
        when(service.cancelarSolicitacao(solicitacaoId)).thenReturn(Optional.of(response));
        
        // Act & Assert
        mockMvc.perform(put("/api/solicitacoes/{id}/cancelar", solicitacaoId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(solicitacaoId.toString()))
            .andExpect(jsonPath("$.status").value("CANCELADA"))
            .andExpect(jsonPath("$.finishedAt").isNotEmpty());
        
        verify(service).cancelarSolicitacao(solicitacaoId);
    }
    
    @Test
    public void testCancelarSolicitacaoNaoPermitido() throws Exception {
        // Arrange
        UUID solicitacaoId = UUID.randomUUID();
        
        when(service.cancelarSolicitacao(solicitacaoId))
            .thenThrow(new IllegalStateException("Solicitação não pode ser cancelada no estado atual: APROVADA"));
        
        // Act & Assert
        mockMvc.perform(put("/api/solicitacoes/{id}/cancelar", solicitacaoId))
            .andExpect(status().isConflict());
        
        verify(service).cancelarSolicitacao(solicitacaoId);
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

