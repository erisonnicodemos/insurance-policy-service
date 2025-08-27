package com.itau.seguros.solicitacao.infrastructure.messaging;

import com.itau.seguros.solicitacao.domain.event.SolicitacaoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Producer responsável por publicar eventos de solicitação no RabbitMQ.
 * 
 * Centraliza a lógica de publicação de eventos, garantindo
 * consistência no roteamento e logging.
 */
@Component
public class SolicitacaoEventProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(SolicitacaoEventProducer.class);
    
    private final RabbitTemplate rabbitTemplate;
    
    @Value("${app.rabbitmq.exchanges.solicitacao}")
    private String solicitacaoExchange;
    
    @Value("${app.rabbitmq.routing-keys.solicitacao-recebida}")
    private String solicitacaoRecebidaRoutingKey;
    
    @Value("${app.rabbitmq.routing-keys.solicitacao-validada}")
    private String solicitacaoValidadaRoutingKey;
    
    @Value("${app.rabbitmq.routing-keys.solicitacao-rejeitada}")
    private String solicitacaoRejeitadaRoutingKey;
    
    public SolicitacaoEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    /**
     * Publica um evento de solicitação no exchange apropriado.
     * 
     * @param event evento a ser publicado
     */
    public void publicarEvento(SolicitacaoEvent event) {
        String routingKey = obterRoutingKey(event.getEventType());
        
        logger.info("Publicando evento {} para solicitação {}", 
                   event.getEventType(), event.getSolicitacaoId());
        
        try {
            rabbitTemplate.convertAndSend(solicitacaoExchange, routingKey, event);
            logger.debug("Evento {} publicado com sucesso", event.getEventType());
        } catch (Exception e) {
            logger.error("Erro ao publicar evento {}: {}", event.getEventType(), e.getMessage(), e);
            throw new RuntimeException("Falha ao publicar evento", e);
        }
    }
    
    /**
     * Determina a routing key baseada no tipo do evento.
     * 
     * @param eventType tipo do evento
     * @return routing key correspondente
     */
    private String obterRoutingKey(String eventType) {
        return switch (eventType) {
            case "SOLICITACAO_RECEBIDA" -> solicitacaoRecebidaRoutingKey;
            case "SOLICITACAO_VALIDADA" -> solicitacaoValidadaRoutingKey;
            case "SOLICITACAO_REJEITADA" -> solicitacaoRejeitadaRoutingKey;
            default -> {
                logger.warn("Tipo de evento desconhecido: {}", eventType);
                yield "unknown";
            }
        };
    }
}

