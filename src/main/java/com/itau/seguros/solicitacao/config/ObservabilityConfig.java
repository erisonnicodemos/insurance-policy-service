package com.itau.seguros.solicitacao.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuração para observabilidade da aplicação.
 * 
 * Define métricas, aspectos e outros componentes relacionados
 * à observabilidade (métricas, logs, tracing).
 */
@Configuration
@EnableAspectJAutoProxy
public class ObservabilityConfig {
    
    /**
     * Configura o aspecto para anotações @Timed.
     * 
     * @param registry registro de métricas
     * @return aspecto configurado
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
    
    /**
     * Configura métricas de carregamento de classes.
     * 
     * @return métricas configuradas
     */
    @Bean
    public ClassLoaderMetrics classLoaderMetrics() {
        return new ClassLoaderMetrics();
    }
    
    /**
     * Configura métricas de garbage collection.
     * 
     * @return métricas configuradas
     */
    @Bean
    public JvmGcMetrics jvmGcMetrics() {
        return new JvmGcMetrics();
    }
    
    /**
     * Configura métricas de memória JVM.
     * 
     * @return métricas configuradas
     */
    @Bean
    public JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics();
    }
    
    /**
     * Configura métricas de threads JVM.
     * 
     * @return métricas configuradas
     */
    @Bean
    public JvmThreadMetrics jvmThreadMetrics() {
        return new JvmThreadMetrics();
    }
    
    /**
     * Configura métricas de processador.
     * 
     * @return métricas configuradas
     */
    @Bean
    public ProcessorMetrics processorMetrics() {
        return new ProcessorMetrics();
    }
}

