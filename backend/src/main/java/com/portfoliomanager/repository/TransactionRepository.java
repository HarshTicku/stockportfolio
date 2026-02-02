package com.portfoliomanager.repository;

import com.portfoliomanager.entity.Transaction;
import com.portfoliomanager.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionId(String transactionId);

    Page<Transaction> findByPortfolioId(Long portfolioId, Pageable pageable);

    List<Transaction> findByPortfolioIdOrderByTransactionDateDesc(Long portfolioId);

    Page<Transaction> findByPortfolioIdAndTransactionType(
            Long portfolioId, 
            TransactionType transactionType, 
            Pageable pageable
    );

    @Query("SELECT t FROM Transaction t WHERE t.portfolio.id = :portfolioId " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "ORDER BY t.transactionDate DESC")
    List<Transaction> findByPortfolioIdAndDateRange(
            @Param("portfolioId") Long portfolioId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT t FROM Transaction t WHERE t.portfolio.id = :portfolioId " +
            "AND t.ticker = :ticker ORDER BY t.transactionDate DESC")
    List<Transaction> findByPortfolioIdAndTicker(
            @Param("portfolioId") Long portfolioId,
            @Param("ticker") String ticker
    );

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.portfolio.id = :portfolioId")
    long countByPortfolioId(@Param("portfolioId") Long portfolioId);
}
