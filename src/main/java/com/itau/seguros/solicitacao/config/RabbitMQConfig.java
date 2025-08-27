package com.itau.seguros.solicitacao.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do RabbitMQ para mensageria.
 * 
 * Define exchanges, filas, bindings e conversores para
 * suportar a arquitetura orientada a eventos.
 */
@Configuration
public class RabbitMQConfig {
    
    @Value("${app.rabbitmq.exchanges.solicitacao}")
    private String solicitacaoExchange;
    
    @Value("${app.rabbitmq.queues.solicitacao-recebida}")
    private String solicitacaoRecebidaQueue;
    
    @Value("${app.rabbitmq.queues.solicitacao-validada}")
    private String solicitacaoValidadaQueue;
    
    @Value("${app.rabbitmq.queues.solicitacao-rejeitada}")
    private String solicitacaoRejeitadaQueue;
    
    @Value("${app.rabbitmq.queues.pagamento-confirmado}")
    private String pagamentoConfirmadoQueue;
    
    @Value("${app.rabbitmq.queues.subscricao-autorizada}")
    private String subscricaoAutorizadaQueue;
    
    @Value("${app.rabbitmq.routing-keys.solicitacao-recebida}")
    private String solicitacaoRecebidaRoutingKey;
    
    @Value("${app.rabbitmq.routing-keys.solicitacao-validada}")
    private String solicitacaoValidadaRoutingKey;
    
    @Value("${app.rabbitmq.routing-keys.solicitacao-rejeitada}")
    private String solicitacaoRejeitadaRoutingKey;
    
    @Value("${app.rabbitmq.routing-keys.pagamento-confirmado}")
    private String pagamentoConfirmadoRoutingKey;
    
    @Value("${app.rabbitmq.routing-keys.subscricao-autorizada}")
    private String subscricaoAutorizadaRoutingKey;
    
    /**
     * Configura o conversor de mensagens para JSON.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
    com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
    objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    return new Jackson2JsonMessageConverter(objectMapper);
    }
    
    /**
     * Configura o template RabbitMQ com conversor JSON.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
    
    /**
     * Configura o exchange para solicitações.
     */
    @Bean
    public TopicExchange solicitacaoExchange() {
        return new TopicExchange(solicitacaoExchange);
    }
    
    /**
     * Configura a fila para solicitações recebidas.
     */
    @Bean
    public Queue solicitacaoRecebidaQueue() {
        return QueueBuilder.durable(solicitacaoRecebidaQueue)
            .withArgument("x-dead-letter-exchange", "")
            .withArgument("x-dead-letter-routing-key", solicitacaoRecebidaQueue + ".dlq")
            .build();
    }
    
    /**
     * Configura a fila para solicitações validadas.
     */
    @Bean
    public Queue solicitacaoValidadaQueue() {
        return QueueBuilder.durable(solicitacaoValidadaQueue)
            .withArgument("x-dead-letter-exchange", "")
            .withArgument("x-dead-letter-routing-key", solicitacaoValidadaQueue + ".dlq")
            .build();
    }
    
    /**
     * Configura a fila para solicitações rejeitadas.
     */
    @Bean
    public Queue solicitacaoRejeitadaQueue() {
        return QueueBuilder.durable(solicitacaoRejeitadaQueue)
            .withArgument("x-dead-letter-exchange", "")
            .withArgument("x-dead-letter-routing-key", solicitacaoRejeitadaQueue + ".dlq")
            .build();
    }
    
    /**
     * Configura a fila para pagamentos confirmados.
     */
    @Bean
    public Queue pagamentoConfirmadoQueue() {
        return QueueBuilder.durable(pagamentoConfirmadoQueue)
            .withArgument("x-dead-letter-exchange", "")
            .withArgument("x-dead-letter-routing-key", pagamentoConfirmadoQueue + ".dlq")
            .build();
    }
    
    /**
     * Configura a fila para subscrições autorizadas.
     */
    @Bean
    public Queue subscricaoAutorizadaQueue() {
        return QueueBuilder.durable(subscricaoAutorizadaQueue)
            .withArgument("x-dead-letter-exchange", "")
            .withArgument("x-dead-letter-routing-key", subscricaoAutorizadaQueue + ".dlq")
            .build();
    }
    
    /**
     * Binding para solicitações recebidas.
     */
    @Bean
    public Binding solicitacaoRecebidaBinding() {
        return BindingBuilder
            .bind(solicitacaoRecebidaQueue())
            .to(solicitacaoExchange())
            .with(solicitacaoRecebidaRoutingKey);
    }
    
    /**
     * Binding para solicitações validadas.
     */
    @Bean
    public Binding solicitacaoValidadaBinding() {
        return BindingBuilder
            .bind(solicitacaoValidadaQueue())
            .to(solicitacaoExchange())
            .with(solicitacaoValidadaRoutingKey);
    }
    
    /**
     * Binding para solicitações rejeitadas.
     */
    @Bean
    public Binding solicitacaoRejeitadaBinding() {
        return BindingBuilder
            .bind(solicitacaoRejeitadaQueue())
            .to(solicitacaoExchange())
            .with(solicitacaoRejeitadaRoutingKey);
    }
    
    /**
     * Binding para pagamentos confirmados.
     */
    @Bean
    public Binding pagamentoConfirmadoBinding() {
        return BindingBuilder
            .bind(pagamentoConfirmadoQueue())
            .to(solicitacaoExchange())
            .with(pagamentoConfirmadoRoutingKey);
    }
    
    /**
     * Binding para subscrições autorizadas.
     */
    @Bean
    public Binding subscricaoAutorizadaBinding() {
        return BindingBuilder
            .bind(subscricaoAutorizadaQueue())
            .to(solicitacaoExchange())
            .with(subscricaoAutorizadaRoutingKey);
    }
}

