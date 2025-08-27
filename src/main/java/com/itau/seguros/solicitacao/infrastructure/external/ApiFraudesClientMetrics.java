package com.itau.seguros.solicitacao.infrastructure.external;

import com.itau.seguros.solicitacao.api.dto.FraudeResponseDTO;
import com.itau.seguros.solicitacao.domain.model.TipoCliente;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Decorator para o cliente da API de Fraudes que adiciona métricas.
 * 
 * Implementa o padrão Decorator para adicionar funcionalidades de
 * observabilidade ao cliente da API de Fraudes sem modificar sua
 * implementação original.
 */
@Component
@Primary
public class ApiFraudesClientMetrics implements ApiFraudesClient {
    
    private final ApiFraudesClient delegate;
    private final Timer apiFraudesTimer;
    private final Map<TipoCliente, Counter> classificacaoCounters = new ConcurrentHashMap<>();
    
    public ApiFraudesClientMetrics(ApiFraudesClient apiFraudesClientMock, 
                                  Timer apiFraudesTimer,
                                  MeterRegistry registry) {
        this.delegate = apiFraudesClientMock;
        this.apiFraudesTimer = apiFraudesTimer;
        
        // Inicializa contadores para cada tipo de cliente
        for (TipoCliente tipo : TipoCliente.values()) {
            classificacaoCounters.put(tipo, Counter.builder("api.fraudes.classificacao")
                .tag("tipo", tipo.name())
                .description("Contagem de classificações por tipo de cliente")
                .register(registry));
        }
    }
    
    @Override
    public FraudeResponseDTO consultarClassificacaoRisco(UUID solicitacaoId, UUID customerId) {
        // Inicia o timer
        Timer.Sample sample = Timer.start();
        
        try {
            // Executa a chamada real
            FraudeResponseDTO response = delegate.consultarClassificacaoRisco(solicitacaoId, customerId);
            
            // Incrementa o contador para o tipo de cliente
            classificacaoCounters.get(response.classification()).increment();
            
            return response;
        } finally {
            // Registra o tempo de execução
            sample.stop(apiFraudesTimer);
        }
    }
}

