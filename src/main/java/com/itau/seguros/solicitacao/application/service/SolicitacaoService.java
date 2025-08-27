package com.itau.seguros.solicitacao.application.service;

import com.itau.seguros.solicitacao.api.dto.FraudeResponseDTO;
import com.itau.seguros.solicitacao.api.dto.SolicitacaoRequestDTO;
import com.itau.seguros.solicitacao.api.dto.SolicitacaoResponseDTO;
import com.itau.seguros.solicitacao.domain.event.SolicitacaoRecebidaEvent;
import com.itau.seguros.solicitacao.domain.event.SolicitacaoRejeitadaEvent;
import com.itau.seguros.solicitacao.domain.event.SolicitacaoValidadaEvent;
import com.itau.seguros.solicitacao.domain.model.SolicitacaoApolice;
import com.itau.seguros.solicitacao.domain.model.StatusSolicitacao;
import com.itau.seguros.solicitacao.domain.repository.SolicitacaoRepository;
import com.itau.seguros.solicitacao.domain.rule.RegraValidacaoCliente;
import com.itau.seguros.solicitacao.infrastructure.external.ApiFraudesClient;
import com.itau.seguros.solicitacao.infrastructure.messaging.SolicitacaoEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Serviço principal para gerenciamento de solicitações de apólice.
 * 
 * Orquestra as operações de negócio, coordenando as interações
 * entre o domínio e a infraestrutura.
 */
@Service
@Transactional
public class SolicitacaoService {
    
    private static final Logger logger = LoggerFactory.getLogger(SolicitacaoService.class);
    
    private final SolicitacaoRepository repository;
    private final ApiFraudesClient apiFraudesClient;
    private final RegraValidacaoCliente regraValidacao;
    private final SolicitacaoEventProducer eventProducer;
    
    public SolicitacaoService(SolicitacaoRepository repository,
                             ApiFraudesClient apiFraudesClient,
                             RegraValidacaoCliente regraValidacao,
                             SolicitacaoEventProducer eventProducer) {
        this.repository = repository;
        this.apiFraudesClient = apiFraudesClient;
        this.regraValidacao = regraValidacao;
        this.eventProducer = eventProducer;
    }
    
    /**
     * Cria uma nova solicitação de apólice.
     * 
     * @param request dados da solicitação
     * @return resposta com dados da solicitação criada
     */
    public SolicitacaoResponseDTO criarSolicitacao(SolicitacaoRequestDTO request) {
        // Configura MDC para tracing
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);
        MDC.put("customerId", request.customerId().toString());
        
        try {
            logger.info("Criando nova solicitação para cliente {}", request.customerId());
            
            // Validações de negócio
            if (!request.isCoberturaValida()) {
                throw new IllegalArgumentException("Soma das coberturas não pode exceder o capital segurado");
            }
            
            if (!request.isValoresCoberturaValidos()) {
                throw new IllegalArgumentException("Todos os valores de cobertura devem ser positivos");
            }
            
            // Cria a entidade
            SolicitacaoApolice solicitacao = new SolicitacaoApolice(
                request.customerId(),
                request.productId(),
                request.category(),
                request.salesChannel(),
                request.paymentMethod(),
                request.totalMonthlyPremiumAmount(),
                request.insuredAmount(),
                request.coverages(),
                request.assistances()
            );
            
            // Persiste
            solicitacao = repository.save(solicitacao);
            
            logger.info("Solicitação {} criada com sucesso", solicitacao.getId());
            
            // Publica evento para processamento assíncrono
            SolicitacaoRecebidaEvent event = new SolicitacaoRecebidaEvent(
                solicitacao.getId(),
                solicitacao.getCustomerId(),
                solicitacao.getProductId(),
                solicitacao.getCategory(),
                solicitacao.getInsuredAmount()
            );
            
            eventProducer.publicarEvento(event);
            
            return mapearParaResponseDTO(solicitacao);
            
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Busca uma solicitação por ID.
     * 
     * @param id ID da solicitação
     * @return solicitação encontrada
     */
    @Transactional(readOnly = true)
    public Optional<SolicitacaoResponseDTO> buscarPorId(UUID id) {
        logger.info("Buscando solicitação por ID: {}", id);
        
        return repository.findByIdWithHistory(id)
            .map(this::mapearParaResponseDTO);
    }
    
    /**
     * Busca todas as solicitações de um cliente.
     * 
     * @param customerId ID do cliente
     * @return lista de solicitações do cliente
     */
    @Transactional(readOnly = true)
    public List<SolicitacaoResponseDTO> buscarPorCustomerId(UUID customerId) {
        logger.info("Buscando solicitações do cliente: {}", customerId);
        
        return repository.findByCustomerIdWithHistory(customerId).stream()
            .map(this::mapearParaResponseDTO)
            .toList();
    }
    
    /**
     * Cancela uma solicitação.
     * 
     * @param id ID da solicitação
     * @return solicitação cancelada
     */
    public Optional<SolicitacaoResponseDTO> cancelarSolicitacao(UUID id) {
        logger.info("Cancelando solicitação: {}", id);
        
        return repository.findById(id)
            .map(solicitacao -> {
                if (!solicitacao.podeCancelar()) {
                    throw new IllegalStateException("Solicitação não pode ser cancelada no estado atual: " + solicitacao.getStatus());
                }
                
                solicitacao.alterarStatus(StatusSolicitacao.CANCELADA, "Cancelamento solicitado pelo cliente");
                solicitacao = repository.save(solicitacao);
                
                logger.info("Solicitação {} cancelada com sucesso", id);
                
                return mapearParaResponseDTO(solicitacao);
            });
    }
    
    /**
     * Processa a validação de uma solicitação com a API de Fraudes.
     * 
     * @param solicitacaoId ID da solicitação
     */
    public void processarValidacao(UUID solicitacaoId) {
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);
        MDC.put("solicitacaoId", solicitacaoId.toString());
        
        try {
            logger.info("Processando validação da solicitação {}", solicitacaoId);
            
            Optional<SolicitacaoApolice> optionalSolicitacao = repository.findById(solicitacaoId);
            if (optionalSolicitacao.isEmpty()) {
                logger.warn("Solicitação {} não encontrada para validação", solicitacaoId);
                return;
            }
            
            SolicitacaoApolice solicitacao = optionalSolicitacao.get();
            
            // Consulta API de Fraudes
            FraudeResponseDTO fraudeResponse = apiFraudesClient.consultarClassificacaoRisco(
                solicitacaoId, solicitacao.getCustomerId()
            );
            
            logger.info("Cliente {} classificado como {}", solicitacao.getCustomerId(), fraudeResponse.classification());
            
            // Aplica regras de validação
            boolean aprovado = regraValidacao.validarCapitalSegurado(
                fraudeResponse.classification(),
                solicitacao.getCategory(),
                solicitacao.getInsuredAmount()
            );
            
            if (aprovado) {
                solicitacao.alterarStatus(StatusSolicitacao.VALIDADO, "Validação aprovada pela API de Fraudes");
                repository.save(solicitacao);
                
                // Publica evento de validação
                SolicitacaoValidadaEvent event = new SolicitacaoValidadaEvent(
                    solicitacao.getId(),
                    solicitacao.getCustomerId(),
                    solicitacao.getProductId(),
                    solicitacao.getCategory(),
                    solicitacao.getInsuredAmount(),
                    fraudeResponse.classification()
                );
                eventProducer.publicarEvento(event);
                
                logger.info("Solicitação {} validada com sucesso", solicitacaoId);
                
            } else {
                String motivoRejeicao = regraValidacao.obterMensagemRejeicao(
                    fraudeResponse.classification(),
                    solicitacao.getCategory(),
                    solicitacao.getInsuredAmount()
                );
                
                solicitacao.alterarStatus(StatusSolicitacao.REJEITADA, motivoRejeicao);
                repository.save(solicitacao);
                
                // Publica evento de rejeição
                SolicitacaoRejeitadaEvent event = new SolicitacaoRejeitadaEvent(
                    solicitacao.getId(),
                    solicitacao.getCustomerId(),
                    solicitacao.getProductId(),
                    solicitacao.getCategory(),
                    solicitacao.getInsuredAmount(),
                    fraudeResponse.classification(),
                    motivoRejeicao
                );
                eventProducer.publicarEvento(event);
                
                logger.info("Solicitação {} rejeitada: {}", solicitacaoId, motivoRejeicao);
            }
            
        } catch (Exception e) {
            logger.error("Erro ao processar validação da solicitação {}", solicitacaoId, e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Mapeia uma entidade SolicitacaoApolice para DTO de resposta.
     * 
     * @param solicitacao entidade a ser mapeada
     * @return DTO de resposta
     */
    private SolicitacaoResponseDTO mapearParaResponseDTO(SolicitacaoApolice solicitacao) {
        List<SolicitacaoResponseDTO.HistoricoStatusDTO> historicoDTO = solicitacao.getHistory().stream()
            .map(h -> new SolicitacaoResponseDTO.HistoricoStatusDTO(
                h.getStatus(),
                h.getTimestamp(),
                h.getObservacao()
            ))
            .toList();
        
        return new SolicitacaoResponseDTO(
            solicitacao.getId(),
            solicitacao.getCustomerId(),
            solicitacao.getProductId(),
            solicitacao.getCategory(),
            solicitacao.getSalesChannel(),
            solicitacao.getPaymentMethod(),
            solicitacao.getStatus(),
            solicitacao.getCreatedAt(),
            solicitacao.getFinishedAt(),
            solicitacao.getTotalMonthlyPremiumAmount(),
            solicitacao.getInsuredAmount(),
            solicitacao.getCoverages(),
            solicitacao.getAssistances(),
            historicoDTO
        );
    }
}

