package com.itau.seguros.solicitacao.domain.repository;

import com.itau.seguros.solicitacao.domain.model.SolicitacaoApolice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para operações de persistência da entidade SolicitacaoApolice.
 * 
 * Utiliza Spring Data JPA para fornecer operações CRUD básicas
 * e consultas customizadas para o domínio de seguros.
 */
@Repository
public interface SolicitacaoRepository extends JpaRepository<SolicitacaoApolice, UUID> {
    
    /**
     * Busca todas as solicitações de um cliente específico.
     * 
     * @param customerId ID do cliente
     * @return lista de solicitações do cliente
     */
    List<SolicitacaoApolice> findByCustomerId(UUID customerId);
    
    /**
     * Busca uma solicitação por ID com o histórico carregado.
     * 
     * @param id ID da solicitação
     * @return solicitação com histórico, se encontrada
     */
    @Query("SELECT s FROM SolicitacaoApolice s LEFT JOIN FETCH s.history WHERE s.id = :id")
    Optional<SolicitacaoApolice> findByIdWithHistory(@Param("id") UUID id);
    
    /**
     * Busca solicitações de um cliente com o histórico carregado.
     * 
     * @param customerId ID do cliente
     * @return lista de solicitações com histórico
     */
    @Query("SELECT s FROM SolicitacaoApolice s LEFT JOIN FETCH s.history WHERE s.customerId = :customerId")
    List<SolicitacaoApolice> findByCustomerIdWithHistory(@Param("customerId") UUID customerId);
    
    /**
     * Verifica se existe alguma solicitação ativa (não finalizada) para um cliente.
     * 
     * @param customerId ID do cliente
     * @return true se existe solicitação ativa
     */
    @Query("SELECT COUNT(s) > 0 FROM SolicitacaoApolice s WHERE s.customerId = :customerId AND s.finishedAt IS NULL")
    boolean existsActiveSolicitacaoByCustomerId(@Param("customerId") UUID customerId);
}

