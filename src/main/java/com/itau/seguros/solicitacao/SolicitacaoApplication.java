package com.itau.seguros.solicitacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Aplicação principal do MVP de Solicitação de Apólice de Seguros.
 * 
 * Este microsserviço gerencia o ciclo de vida das solicitações de apólice,
 * desde o recebimento até a aprovação/rejeição, utilizando arquitetura
 * orientada a eventos (Event-Driven Architecture).
 * 
 * Funcionalidades principais:
 * - Recebimento e persistência de solicitações de apólice
 * - Validação com API de Fraudes (mockada)
 * - Gerenciamento de estados do ciclo de vida
 * - Publicação e consumo de eventos via RabbitMQ
 * - Observabilidade com logs, métricas e tracing básico
 * 
 * @author Manus AI
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAsync
public class SolicitacaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SolicitacaoApplication.class, args);
    }
}

