package com.portfoliomanager.repository;

import com.portfoliomanager.entity.Asset;
import com.portfoliomanager.entity.AssetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    Optional<Asset> findByAssetId(String assetId);

    List<Asset> findByPortfolioId(Long portfolioId);

    Page<Asset> findByPortfolioId(Long portfolioId, Pageable pageable);

    Page<Asset> findByAssetType(AssetType assetType, Pageable pageable);

    List<Asset> findByTicker(String ticker);

    Optional<Asset> findByPortfolioIdAndTicker(Long portfolioId, String ticker);

    boolean existsByPortfolioIdAndTicker(Long portfolioId, String ticker);

    @Query("SELECT a FROM Asset a WHERE a.portfolio.id = :portfolioId AND a.assetType = :assetType")
    List<Asset> findByPortfolioIdAndAssetType(@Param("portfolioId") Long portfolioId, 
                                               @Param("assetType") AssetType assetType);

    @Query("SELECT COUNT(a) FROM Asset a WHERE a.portfolio.id = :portfolioId")
    long countByPortfolioId(@Param("portfolioId") Long portfolioId);

    @Query("SELECT DISTINCT a.ticker FROM Asset a ORDER BY a.ticker")
    List<String> findAllDistinctTickers();
}
