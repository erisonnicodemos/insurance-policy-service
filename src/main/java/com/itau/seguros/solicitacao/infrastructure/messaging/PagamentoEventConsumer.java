package com.itau.seguros.solicitacao.infrastructure.messaging;

import com.itau.seguros.solicitacao.domain.model.SolicitacaoApolice;
import com.itau.seguros.solicitacao.domain.model.StatusSolicitacao;
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
 * Consumer responsável por processar eventos de pagamento.
 * 
 * Escuta a fila de eventos de pagamento confirmado e atualiza
 * o estado da solicitação correspondente.
 */
@Component
public class PagamentoEventConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(PagamentoEventConsumer.class);
    
    private final SolicitacaoRepository repository;
    private final SolicitacaoEventProducer eventProducer;
    
    public PagamentoEventConsumer(SolicitacaoRepository repository, SolicitacaoEventProducer eventProducer) {
        this.repository = repository;
        this.eventProducer = eventProducer;
    }
    
    /**
     * Processa eventos de pagamento confirmado.
     * 
     * @param event evento recebido
     */
    @RabbitListener(queues = "${app.rabbitmq.queues.pagamento-confirmado}")
    @Transactional
    public void processarPagamentoConfirmado(Map<String, Object> event) {
        UUID solicitacaoId = UUID.fromString((String) event.get("solicitacaoId"));
        String transactionId = UUID.randomUUID().toString();
        
        MDC.put("transactionId", transactionId);
        MDC.put("solicitacaoId", solicitacaoId.toString());
        
        try {
            logger.info("Recebido evento de pagamento confirmado para solicitação: {}", solicitacaoId);
            
            Optional<SolicitacaoApolice> optionalSolicitacao = repository.findById(solicitacaoId);
            if (optionalSolicitacao.isEmpty()) {
                logger.warn("Solicitação {} não encontrada para processamento de pagamento", solicitacaoId);
                return;
            }
            
            SolicitacaoApolice solicitacao = optionalSolicitacao.get();
            
            // Verifica se a solicitação está no estado correto para receber pagamento
            if (solicitacao.getStatus() != StatusSolicitacao.VALIDADO && 
                solicitacao.getStatus() != StatusSolicitacao.PENDENTE) {
                logger.warn("Solicitação {} está no estado {}, não pode processar pagamento", 
                           solicitacaoId, solicitacao.getStatus());
                return;
            }
            
            // Atualiza o estado para PENDENTE (aguardando subscrição)
            // Se já estiver PENDENTE, mantém o estado
            if (solicitacao.getStatus() == StatusSolicitacao.VALIDADO) {
                solicitacao.alterarStatus(StatusSolicitacao.PENDENTE, "Pagamento confirmado, aguardando subscrição");
                repository.save(solicitacao);
                logger.info("Solicitação {} atualizada para PENDENTE após confirmação de pagamento", solicitacaoId);
            } else {
                logger.info("Solicitação {} já está PENDENTE, aguardando subscrição", solicitacaoId);
            }
            
            // Em um cenário real, verificaria se tanto pagamento quanto subscrição estão confirmados
            // para então aprovar a solicitação
            
        } catch (Exception e) {
            logger.error("Erro ao processar evento de pagamento confirmado: {}", e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}

