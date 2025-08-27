package com.itau.seguros.solicitacao.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração de métricas customizadas para observabilidade.
 * 
 * Define contadores, temporizadores e outros tipos de métricas
 * específicas para o domínio de seguros.
 */
@Configuration
public class MetricasConfig {
    
    /**
     * Contador de solicitações recebidas.
     */
    @Bean
    public Counter solicitacoesRecebidasCounter(MeterRegistry registry) {
        return Counter.builder("solicitacao.recebidas")
            .description("Total de solicitações de apólice recebidas")
            .register(registry);
    }
    
    /**
     * Contador de solicitações validadas.
     */
    @Bean
    public Counter solicitacoesValidadasCounter(MeterRegistry registry) {
        return Counter.builder("solicitacao.validadas")
            .description("Total de solicitações validadas pela API de Fraudes")
            .register(registry);
    }
    
    /**
     * Contador de solicitações rejeitadas.
     */
    @Bean
    public Counter solicitacoesRejeitadasCounter(MeterRegistry registry) {
        return Counter.builder("solicitacao.rejeitadas")
            .description("Total de solicitações rejeitadas")
            .register(registry);
    }
    
    /**
     * Contador de solicitações aprovadas.
     */
    @Bean
    public Counter solicitacoesAprovadasCounter(MeterRegistry registry) {
        return Counter.builder("solicitacao.aprovadas")
            .description("Total de solicitações aprovadas")
            .register(registry);
    }
    
    /**
     * Contador de solicitações canceladas.
     */
    @Bean
    public Counter solicitacoesCanceladasCounter(MeterRegistry registry) {
        return Counter.builder("solicitacao.canceladas")
            .description("Total de solicitações canceladas")
            .register(registry);
    }
    
    /**
     * Temporizador para consulta à API de Fraudes.
     */
    @Bean
    public Timer apiFraudesTimer(MeterRegistry registry) {
        return Timer.builder("api.fraudes.tempo")
            .description("Tempo de resposta da API de Fraudes")
            .register(registry);
    }
    
    /**
     * Temporizador para processamento de eventos.
     */
    @Bean
    public Timer processamentoEventosTimer(MeterRegistry registry) {
        return Timer.builder("eventos.processamento.tempo")
            .description("Tempo de processamento de eventos")
            .register(registry);
    }
}

