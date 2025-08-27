package com.itau.seguros.solicitacao.infrastructure.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Mock do cliente AWS SQS para demonstração de integração com AWS.
 * 
 * Em um ambiente real, esta classe utilizaria o AWS SDK para
 * enviar mensagens para filas SQS. Para o MVP, simula a operação
 * apenas com logs.
 */
@Component
public class AwsSqsClient {
    
    private static final Logger logger = LoggerFactory.getLogger(AwsSqsClient.class);
    
    /**
     * Simula o envio de uma mensagem para uma fila SQS.
     * 
     * @param queueUrl URL da fila SQS
     * @param message mensagem a ser enviada
     * @return ID da mensagem (simulado)
     */
    public String enviarMensagem(String queueUrl, String message) {
        logger.info("Simulando envio de mensagem para SQS - Queue: {}", queueUrl);
        logger.debug("Conteúdo da mensagem: {}", message);
        
        // Simula um tempo de resposta da AWS
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String messageId = java.util.UUID.randomUUID().toString();
        logger.info("Mensagem enviada com sucesso - MessageId: {}", messageId);
        
        return messageId;
    }
    
    /**
     * Simula o recebimento de mensagens de uma fila SQS.
     * 
     * @param queueUrl URL da fila SQS
     * @param maxMessages número máximo de mensagens a receber
     * @return lista de mensagens (sempre vazia no mock)
     */
    public java.util.List<String> receberMensagens(String queueUrl, int maxMessages) {
        logger.info("Simulando recebimento de mensagens do SQS - Queue: {}, Max: {}", queueUrl, maxMessages);
        
        // No mock, sempre retorna lista vazia
        // Em um cenário real, retornaria as mensagens da fila
        return java.util.Collections.emptyList();
    }
}

