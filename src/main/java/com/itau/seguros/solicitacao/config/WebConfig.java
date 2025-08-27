package com.itau.seguros.solicitacao.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração para aspectos web da aplicação.
 * 
 * Define configurações como CORS, interceptors, formatters, etc.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    /**
     * Configura CORS para permitir acesso de origens externas.
     * 
     * @param registry registro de CORS
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .maxAge(3600);
    }
}

