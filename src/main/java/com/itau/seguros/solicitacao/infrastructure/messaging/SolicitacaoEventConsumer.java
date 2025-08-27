package com.itau.seguros.solicitacao.infrastructure.messaging;

import com.itau.seguros.solicitacao.application.service.SolicitacaoService;
import com.itau.seguros.solicitacao.domain.event.SolicitacaoRecebidaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer responsável por processar eventos de solicitação do RabbitMQ.
 * 
 * Escuta as filas de eventos e aciona os serviços apropriados
 * para processamento assíncrono.
 */
@Component
public class SolicitacaoEventConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(SolicitacaoEventConsumer.class);
    
    private final SolicitacaoService solicitacaoService;
    
    public SolicitacaoEventConsumer(SolicitacaoService solicitacaoService) {
        this.solicitacaoService = solicitacaoService;
    }
    
    /**
     * Processa eventos de solicitação recebida.
     * 
     * @param event evento recebido
     */
    @RabbitListener(queues = "${app.rabbitmq.queues.solicitacao-recebida}")
    public void processarSolicitacaoRecebida(SolicitacaoRecebidaEvent event) {
        logger.info("Recebido evento de solicitação recebida: {}", event.getSolicitacaoId());
        
        try {
            solicitacaoService.processarValidacao(event.getSolicitacaoId());
        } catch (Exception e) {
            logger.error("Erro ao processar evento de solicitação recebida: {}", e.getMessage(), e);
            // Em um cenário real, poderia implementar retry ou DLQ
            throw e;
        }
    }
    
    // Outros listeners para diferentes tipos de eventos podem ser adicionados aqui
    // Por exemplo, para eventos de pagamento confirmado e subscrição autorizada
}

