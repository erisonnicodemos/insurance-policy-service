package com.itau.seguros.solicitacao.api.controller;

import com.itau.seguros.solicitacao.api.dto.SolicitacaoRequestDTO;
import com.itau.seguros.solicitacao.api.dto.SolicitacaoResponseDTO;
import com.itau.seguros.solicitacao.application.service.SolicitacaoService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST para operações de solicitação de apólice.
 * 
 * Expõe endpoints para criar, consultar e cancelar solicitações,
 * seguindo os princípios RESTful.
 */
@RestController
@RequestMapping("/api/solicitacoes")
public class SolicitacaoController {
    
    private static final Logger logger = LoggerFactory.getLogger(SolicitacaoController.class);
    
    private final SolicitacaoService service;
    
    public SolicitacaoController(SolicitacaoService service) {
        this.service = service;
    }
    
    /**
     * Cria uma nova solicitação de apólice.
     * 
     * @param request dados da solicitação
     * @return solicitação criada
     */
    @PostMapping
    @Timed(value = "solicitacao.criar", description = "Tempo para criar uma solicitação")
    public ResponseEntity<SolicitacaoResponseDTO> criar(@Valid @RequestBody SolicitacaoRequestDTO request) {
        logger.info("Recebida requisição para criar solicitação");
        
        try {
            SolicitacaoResponseDTO response = service.criarSolicitacao(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao criar solicitação: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Erro ao criar solicitação", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao processar solicitação");
        }
    }
    
    /**
     * Busca uma solicitação por ID.
     * 
     * @param id ID da solicitação
     * @return solicitação encontrada
     */
    @GetMapping("/{id}")
    @Timed(value = "solicitacao.buscarPorId", description = "Tempo para buscar uma solicitação por ID")
    public ResponseEntity<SolicitacaoResponseDTO> buscarPorId(@PathVariable UUID id) {
        logger.info("Buscando solicitação por ID: {}", id);
        
        return service.buscarPorId(id)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> {
                logger.warn("Solicitação não encontrada: {}", id);
                return new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada");
            });
    }
    
    /**
     * Busca solicitações por ID do cliente.
     * 
     * @param customerId ID do cliente
     * @return lista de solicitações do cliente
     */
    @GetMapping
    @Timed(value = "solicitacao.buscarPorCustomerId", description = "Tempo para buscar solicitações por ID do cliente")
    public ResponseEntity<List<SolicitacaoResponseDTO>> buscarPorCustomerId(@RequestParam UUID customerId) {
        logger.info("Buscando solicitações do cliente: {}", customerId);
        
        List<SolicitacaoResponseDTO> solicitacoes = service.buscarPorCustomerId(customerId);
        return ResponseEntity.ok(solicitacoes);
    }
    
    /**
     * Cancela uma solicitação.
     * 
     * @param id ID da solicitação
     * @return solicitação cancelada
     */
    @PutMapping("/{id}/cancelar")
    @Timed(value = "solicitacao.cancelar", description = "Tempo para cancelar uma solicitação")
    public ResponseEntity<SolicitacaoResponseDTO> cancelar(@PathVariable UUID id) {
        logger.info("Cancelando solicitação: {}", id);
        
        try {
            return service.cancelarSolicitacao(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> {
                    logger.warn("Solicitação não encontrada para cancelamento: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada");
                });
        } catch (IllegalStateException e) {
            logger.warn("Não é possível cancelar a solicitação {}: {}", id, e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            logger.error("Erro ao cancelar solicitação {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao processar cancelamento");
        }
    }
}

