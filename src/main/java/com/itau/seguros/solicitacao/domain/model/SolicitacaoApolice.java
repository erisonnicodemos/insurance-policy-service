package com.itau.seguros.solicitacao.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Entidade principal que representa uma solicitação de apólice de seguro.
 * 
 * Esta é a entidade central do domínio, contendo todas as informações
 * necessárias para o processamento de uma solicitação de seguro,
 * incluindo dados do cliente, produto, coberturas e histórico de estados.
 */
@Entity
@Table(name = "solicitacao_apolice")
public class SolicitacaoApolice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Column(name = "product_id", nullable = false)
    private String productId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private CategoriaSeguro category;
    
    @Column(name = "sales_channel", nullable = false)
    private String salesChannel;
    
    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;
    
    @Column(name = "total_monthly_premium_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalMonthlyPremiumAmount;
    
    @Column(name = "insured_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal insuredAmount;
    
    @ElementCollection
    @CollectionTable(name = "solicitacao_coberturas", joinColumns = @JoinColumn(name = "solicitacao_id"))
    @MapKeyColumn(name = "cobertura_nome")
    @Column(name = "cobertura_valor", precision = 12, scale = 2)
    private Map<String, BigDecimal> coverages = new HashMap<>();
    
    @ElementCollection
    @CollectionTable(name = "solicitacao_assistencias", joinColumns = @JoinColumn(name = "solicitacao_id"))
    @Column(name = "assistencia")
    private List<String> assistances = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusSolicitacao status;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "finished_at")
    private LocalDateTime finishedAt;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "solicitacao_id")
    @OrderBy("timestamp ASC")
    private List<HistoricoStatus> history = new ArrayList<>();
    
    // Construtor padrão para JPA
    protected SolicitacaoApolice() {}
    
    /**
     * Construtor para criar uma nova solicitação de apólice.
     * 
     * @param customerId ID do cliente
     * @param productId ID do produto
     * @param category categoria do seguro
     * @param salesChannel canal de vendas
     * @param paymentMethod forma de pagamento
     * @param totalMonthlyPremiumAmount valor mensal do prêmio
     * @param insuredAmount valor do capital segurado
     * @param coverages coberturas contratadas
     * @param assistances assistências contratadas
     */
    public SolicitacaoApolice(UUID customerId, String productId, CategoriaSeguro category,
                             String salesChannel, String paymentMethod,
                             BigDecimal totalMonthlyPremiumAmount, BigDecimal insuredAmount,
                             Map<String, BigDecimal> coverages, List<String> assistances) {
        this.customerId = customerId;
        this.productId = productId;
        this.category = category;
        this.salesChannel = salesChannel;
        this.paymentMethod = paymentMethod;
        this.totalMonthlyPremiumAmount = totalMonthlyPremiumAmount;
        this.insuredAmount = insuredAmount;
        this.coverages = new HashMap<>(coverages);
        this.assistances = new ArrayList<>(assistances);
        this.status = StatusSolicitacao.RECEBIDO;
        this.createdAt = LocalDateTime.now();
        
        // Adiciona o primeiro registro no histórico
        adicionarHistorico(StatusSolicitacao.RECEBIDO, "Solicitação recebida");
    }
    
    /**
     * Altera o status da solicitação, validando se a transição é permitida.
     * 
     * @param novoStatus o novo status
     * @param observacao observação sobre a mudança
     * @throws IllegalStateException se a transição não for permitida
     */
    public void alterarStatus(StatusSolicitacao novoStatus, String observacao) {
        if (!this.status.podeTransicionarPara(novoStatus)) {
            throw new IllegalStateException(
                String.format("Transição de %s para %s não é permitida", this.status, novoStatus)
            );
        }
        
        this.status = novoStatus;
        adicionarHistorico(novoStatus, observacao);
        
        // Define data de finalização para estados finais
        if (novoStatus.isEstadoFinal() && this.finishedAt == null) {
            this.finishedAt = LocalDateTime.now();
        }
    }
    
    /**
     * Adiciona um registro ao histórico de mudanças de status.
     * 
     * @param status o status
     * @param observacao observação sobre a mudança
     */
    private void adicionarHistorico(StatusSolicitacao status, String observacao) {
        this.history.add(new HistoricoStatus(status, LocalDateTime.now(), observacao));
    }
    
    /**
     * Verifica se a solicitação pode ser cancelada.
     * 
     * @return true se pode ser cancelada
     */
    public boolean podeCancelar() {
        return this.status != StatusSolicitacao.APROVADA && !this.status.isEstadoFinal();
    }
    
    // Getters
    public UUID getId() {
        return id;
    }
    
    public UUID getCustomerId() {
        return customerId;
    }
    
    public String getProductId() {
        return productId;
    }
    
    public CategoriaSeguro getCategory() {
        return category;
    }
    
    public String getSalesChannel() {
        return salesChannel;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public BigDecimal getTotalMonthlyPremiumAmount() {
        return totalMonthlyPremiumAmount;
    }
    
    public BigDecimal getInsuredAmount() {
        return insuredAmount;
    }
    
    public Map<String, BigDecimal> getCoverages() {
        return Collections.unmodifiableMap(coverages);
    }
    
    public List<String> getAssistances() {
        return Collections.unmodifiableList(assistances);
    }
    
    public StatusSolicitacao getStatus() {
        return status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }
    
    public List<HistoricoStatus> getHistory() {
        return Collections.unmodifiableList(history);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SolicitacaoApolice that = (SolicitacaoApolice) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "SolicitacaoApolice{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", productId='" + productId + '\'' +
                ", category=" + category +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}

