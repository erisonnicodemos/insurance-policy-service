package com.itau.seguros.solicitacao.infrastructure.messaging;

import com.itau.seguros.solicitacao.domain.event.SolicitacaoRejeitadaEvent;
import com.itau.seguros.solicitacao.domain.model.SolicitacaoApolice;
import com.itau.seguros.solicitacao.domain.model.StatusSolicitacao;
import com.itau.seguros.solicitacao.domain.model.TipoCliente;
import com.itau.seguros.solicitacao.domain.repository.SolicitacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Consumer responsável por processar eventos de subscrição.
 * 
 * Escuta a fila de eventos de subscrição autorizada e atualiza
 * o estado da solicitação correspondente.
 */
@Component
public class SubscricaoEventConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(SubscricaoEventConsumer.class);
    
    private final SolicitacaoRepository repository;
    private final SolicitacaoEventProducer eventProducer;
    
    public SubscricaoEventConsumer(SolicitacaoRepository repository, SolicitacaoEventProducer eventProducer) {
        this.repository = repository;
        this.eventProducer = eventProducer;
    }
    
    /**
     * Processa eventos de subscrição autorizada.
     * 
     * @param event evento recebido
     */
    @RabbitListener(queues = "${app.rabbitmq.queues.subscricao-autorizada}")
    @Transactional
    public void processarSubscricaoAutorizada(Map<String, Object> event) {
        UUID solicitacaoId = UUID.fromString((String) event.get("solicitacaoId"));
        Boolean autorizado = (Boolean) event.get("autorizado");
        String transactionId = UUID.randomUUID().toString();
        
        MDC.put("transactionId", transactionId);
        MDC.put("solicitacaoId", solicitacaoId.toString());
        
        try {
            logger.info("Recebido evento de subscrição para solicitação: {}, autorizado: {}", 
                       solicitacaoId, autorizado);
            
            Optional<SolicitacaoApolice> optionalSolicitacao = repository.findById(solicitacaoId);
            if (optionalSolicitacao.isEmpty()) {
                logger.warn("Solicitação {} não encontrada para processamento de subscrição", solicitacaoId);
                return;
            }
            
            SolicitacaoApolice solicitacao = optionalSolicitacao.get();
            
            // Verifica se a solicitação está no estado correto para receber subscrição
            if (solicitacao.getStatus() != StatusSolicitacao.PENDENTE) {
                logger.warn("Solicitação {} está no estado {}, não pode processar subscrição", 
                           solicitacaoId, solicitacao.getStatus());
                return;
            }
            
            if (autorizado) {
                // Subscrição autorizada, aprova a solicitação
                solicitacao.alterarStatus(StatusSolicitacao.APROVADA, "Subscrição autorizada");
                repository.save(solicitacao);
                logger.info("Solicitação {} APROVADA após autorização de subscrição", solicitacaoId);
                
                // Em um cenário real, publicaria um evento de aprovação
                // eventProducer.publicarEvento(new SolicitacaoAprovadaEvent(...));
                
            } else {
                // Subscrição negada, rejeita a solicitação
                String motivoRejeicao = (String) event.getOrDefault("motivoRejeicao", 
                                                                  "Subscrição negada pelo subscritor");
                
                solicitacao.alterarStatus(StatusSolicitacao.REJEITADA, motivoRejeicao);
                repository.save(solicitacao);
                logger.info("Solicitação {} REJEITADA: {}", solicitacaoId, motivoRejeicao);
                
                // Publica evento de rejeição
                SolicitacaoRejeitadaEvent rejeitadaEvent = new SolicitacaoRejeitadaEvent(
                    solicitacao.getId(),
                    solicitacao.getCustomerId(),
                    solicitacao.getProductId(),
                    solicitacao.getCategory(),
                    solicitacao.getInsuredAmount(),
                    TipoCliente.REGULAR, // Simplificação para o MVP
                    motivoRejeicao
                );
                eventProducer.publicarEvento(rejeitadaEvent);
            }
            
        } catch (Exception e) {
            logger.error("Erro ao processar evento de subscrição: {}", e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}

