package com.itau.seguros.solicitacao.api.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller para verificações de saúde da aplicação.
 * 
 * Fornece endpoints para monitoramento da saúde dos componentes
 * da aplicação, incluindo conectividade com RabbitMQ.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {
    
    private final RabbitTemplate rabbitTemplate;
    
    public HealthController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    /**
     * Endpoint básico de health check.
     * 
     * @return status da aplicação
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", LocalDateTime.now());
        status.put("service", "solicitacao-apolice-mvp");
        status.put("version", "1.0.0");
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * Endpoint para verificar conectividade com RabbitMQ.
     * 
     * @return status da conectividade
     */
    @GetMapping("/rabbitmq")
    public ResponseEntity<Map<String, Object>> healthRabbitMQ() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // Tenta obter informações da conexão
            rabbitTemplate.getConnectionFactory().createConnection().close();
            status.put("status", "UP");
            status.put("message", "RabbitMQ conectado");
        } catch (Exception e) {
            status.put("status", "DOWN");
            status.put("message", "Erro na conexão com RabbitMQ: " + e.getMessage());
            return ResponseEntity.status(503).body(status);
        }
        
        status.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(status);
    }
}

