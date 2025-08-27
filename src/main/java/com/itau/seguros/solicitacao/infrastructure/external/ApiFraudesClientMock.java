package com.itau.seguros.solicitacao.infrastructure.external;

import com.itau.seguros.solicitacao.api.dto.FraudeResponseDTO;
import com.itau.seguros.solicitacao.domain.model.TipoCliente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementação mockada do cliente da API de Fraudes.
 * 
 * Simula o comportamento da API externa, retornando classificações
 * de risco pré-definidas com base no ID do cliente.
 */
@Component
public class ApiFraudesClientMock implements ApiFraudesClient {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiFraudesClientMock.class);
    
    // Mapa para simular classificações fixas por ID de cliente
    private static final Map<String, TipoCliente> CLASSIFICACOES_FIXAS = new ConcurrentHashMap<>();
    
    static {
        // Inicializa com alguns IDs de exemplo para testes
        CLASSIFICACOES_FIXAS.put("adc56d77-348c-4bf0-908f-22d402ee715c", TipoCliente.REGULAR);
        CLASSIFICACOES_FIXAS.put("7c2a27ba-71ef-4dd8-a3cf-5e094316ffd8", TipoCliente.ALTO_RISCO);
        CLASSIFICACOES_FIXAS.put("b5e8f8a1-c2d3-4e5f-a6b7-c8d9e0f1a2b3", TipoCliente.PREFERENCIAL);
        CLASSIFICACOES_FIXAS.put("d4e5f6a7-b8c9-0d1e-2f3a-4b5c6d7e8f9a", TipoCliente.SEM_INFORMACAO);
    }
    
    @Override
    public FraudeResponseDTO consultarClassificacaoRisco(UUID solicitacaoId, UUID customerId) {
        logger.info("Consultando classificação de risco para solicitação {} e cliente {}", solicitacaoId, customerId);
        
        // Determina a classificação com base no ID do cliente ou aleatoriamente
        TipoCliente classificacao = CLASSIFICACOES_FIXAS.getOrDefault(
            customerId.toString(),
            obterClassificacaoAleatoria()
        );
        
        // Simula um tempo de resposta da API externa
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Cria ocorrências de exemplo para clientes de alto risco
        List<FraudeResponseDTO.OcorrenciaDTO> ocorrencias = new ArrayList<>();
        if (classificacao == TipoCliente.ALTO_RISCO) {
            ocorrencias.add(new FraudeResponseDTO.OcorrenciaDTO(
                UUID.randomUUID(),
                "78900069",
                "FRAUD",
                "Attempted Fraudulent transaction",
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now().minusDays(30)
            ));
            
            ocorrencias.add(new FraudeResponseDTO.OcorrenciaDTO(
                UUID.randomUUID(),
                "104445569",
                "SUSPICION",
                "Unusual activity flagged for review",
                LocalDateTime.now().minusDays(60),
                LocalDateTime.now().minusDays(60)
            ));
        }
        
        logger.info("Cliente {} classificado como {}", customerId, classificacao);
        
        return new FraudeResponseDTO(
            solicitacaoId,
            customerId,
            LocalDateTime.now(),
            classificacao,
            ocorrencias
        );
    }
    
    /**
     * Retorna uma classificação aleatória para clientes não mapeados.
     * 
     * @return classificação aleatória
     */
    private TipoCliente obterClassificacaoAleatoria() {
        TipoCliente[] tipos = TipoCliente.values();
        int indice = (int) (Math.random() * tipos.length);
        return tipos[indice];
    }
}

