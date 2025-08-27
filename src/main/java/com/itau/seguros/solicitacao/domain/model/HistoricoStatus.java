package com.itau.seguros.solicitacao.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade que representa um registro no histórico de mudanças de status
 * de uma solicitação de apólice.
 * 
 * Cada mudança de status é registrada com timestamp para auditoria
 * e rastreabilidade do ciclo de vida da solicitação.
 */
@Entity
@Table(name = "historico_status")
public class HistoricoStatus {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusSolicitacao status;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "observacao")
    private String observacao;
    
    // Construtor padrão para JPA
    protected HistoricoStatus() {}
    
    /**
     * Construtor para criar um novo registro de histórico.
     * 
     * @param status o status da solicitação
     * @param timestamp o momento da mudança
     */
    public HistoricoStatus(StatusSolicitacao status, LocalDateTime timestamp) {
        this.status = status;
        this.timestamp = timestamp;
    }
    
    /**
     * Construtor para criar um novo registro de histórico com observação.
     * 
     * @param status o status da solicitação
     * @param timestamp o momento da mudança
     * @param observacao observação adicional sobre a mudança
     */
    public HistoricoStatus(StatusSolicitacao status, LocalDateTime timestamp, String observacao) {
        this.status = status;
        this.timestamp = timestamp;
        this.observacao = observacao;
    }
    
    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public StatusSolicitacao getStatus() {
        return status;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public String getObservacao() {
        return observacao;
    }
    
    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoricoStatus that = (HistoricoStatus) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "HistoricoStatus{" +
                "id=" + id +
                ", status=" + status +
                ", timestamp=" + timestamp +
                ", observacao='" + observacao + '\'' +
                '}';
    }
}

