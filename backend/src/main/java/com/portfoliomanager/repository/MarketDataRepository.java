package com.portfoliomanager.repository;

import com.portfoliomanager.entity.MarketData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MarketDataRepository extends JpaRepository<MarketData, Long> {

    Page<MarketData> findByTicker(String ticker, Pageable pageable);

    List<MarketData> findByTickerOrderByDateDesc(String ticker);

    Page<MarketData> findByTickerAndDateBetween(
            String ticker,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    List<MarketData> findByTickerAndDateBetweenOrderByDateAsc(
            String ticker,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("SELECT m FROM MarketData m WHERE m.ticker = :ticker ORDER BY m.date DESC LIMIT 1")
    Optional<MarketData> findLatestByTicker(@Param("ticker") String ticker);

    @Query("SELECT DISTINCT m.ticker FROM MarketData m ORDER BY m.ticker")
    List<String> findAllTickers();

    @Query("SELECT m FROM MarketData m WHERE m.ticker = :ticker AND m.date = " +
            "(SELECT MAX(m2.date) FROM MarketData m2 WHERE m2.ticker = :ticker)")
    Optional<MarketData> findMostRecentByTicker(@Param("ticker") String ticker);

    @Query("SELECT m FROM MarketData m WHERE m.date = :date ORDER BY m.ticker")
    List<MarketData> findByDate(@Param("date") LocalDate date);

    boolean existsByTickerAndDate(String ticker, LocalDate date);
}
