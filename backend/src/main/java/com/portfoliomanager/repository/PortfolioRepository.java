package com.portfoliomanager.repository;

import com.portfoliomanager.entity.Portfolio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    Optional<Portfolio> findByPortfolioId(String portfolioId);

    boolean existsByPortfolioId(String portfolioId);

    Page<Portfolio> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT p FROM Portfolio p WHERE LOWER(p.portfolioName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Portfolio> searchByName(@Param("name") String name, Pageable pageable);

    @Query("SELECT p FROM Portfolio p LEFT JOIN FETCH p.assets WHERE p.portfolioId = :portfolioId")
    Optional<Portfolio> findByPortfolioIdWithAssets(@Param("portfolioId") String portfolioId);
}
