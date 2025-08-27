package com.itau.seguros.solicitacao.infrastructure.messaging;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Aspecto para coletar métricas de processamento de eventos.
 * 
 * Utiliza AOP para interceptar chamadas aos métodos de processamento
 * de eventos e registrar métricas de tempo e contagem.
 */
@Aspect
@Component
public class EventMetricsAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(EventMetricsAspect.class);
    
    private final Timer processamentoEventosTimer;
    private final Map<String, Counter> eventosProcessadosCounters = new ConcurrentHashMap<>();
    private final MeterRegistry registry;
    
    public EventMetricsAspect(Timer processamentoEventosTimer, MeterRegistry registry) {
        this.processamentoEventosTimer = processamentoEventosTimer;
        this.registry = registry;
    }
    
    /**
     * Intercepta chamadas aos métodos de processamento de eventos.
     * 
     * @param joinPoint ponto de junção
     * @return resultado da execução do método
     * @throws Throwable se ocorrer erro na execução
     */
    @Around("execution(* com.itau.seguros.solicitacao.infrastructure.messaging.*EventConsumer.processar*(..))")
    public Object aroundEventProcessing(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String eventType = methodName.replace("processar", "");
        
        // Obtém ou cria contador para o tipo de evento
        Counter counter = eventosProcessadosCounters.computeIfAbsent(eventType, 
            type -> Counter.builder("eventos.processados")
                .tag("tipo", type)
                .description("Contagem de eventos processados por tipo")
                .register(registry));
        
        // Inicia o timer
        Timer.Sample sample = Timer.start();
        
        try {
            // Executa o método
            Object result = joinPoint.proceed();
            
            // Incrementa o contador
            counter.increment();
            
            return result;
        } catch (Exception e) {
            // Registra erro
            Counter.builder("eventos.erros")
                .tag("tipo", eventType)
                .tag("erro", e.getClass().getSimpleName())
                .description("Contagem de erros no processamento de eventos")
                .register(registry)
                .increment();
            
            throw e;
        } finally {
            // Registra o tempo de execução
            sample.stop(processamentoEventosTimer);
        }
    }
}

