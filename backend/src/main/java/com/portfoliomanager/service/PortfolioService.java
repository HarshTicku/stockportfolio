package com.portfoliomanager.service;

import com.portfoliomanager.dto.*;
import com.portfoliomanager.entity.*;
import com.portfoliomanager.exception.*;
import com.portfoliomanager.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final AssetRepository assetRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public PortfolioDTO createPortfolio(PortfolioDTO dto) {
        log.info("Creating portfolio: {}", dto.getPortfolioName());
        
        Portfolio portfolio = Portfolio.builder()
                .portfolioName(dto.getPortfolioName())
                .description(dto.getDescription())
                .baseCurrency(dto.getBaseCurrency() != null ? dto.getBaseCurrency() : "USD")
                .cashBalance(dto.getCashBalance() != null ? dto.getCashBalance() : BigDecimal.ZERO)
                .totalValue(dto.getCashBalance() != null ? dto.getCashBalance() : BigDecimal.ZERO)
                .build();

        Portfolio saved = portfolioRepository.save(portfolio);
        log.info("Portfolio created with ID: {}", saved.getPortfolioId());
        
        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public Page<PortfolioDTO> getAllPortfolios(Pageable pageable) {
        return portfolioRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public PortfolioDTO getPortfolioById(String portfolioId) {
        Portfolio portfolio = portfolioRepository.findByPortfolioId(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "portfolioId", portfolioId));
        return mapToDTO(portfolio);
    }

    @Transactional(readOnly = true)
    public PortfolioSummaryDTO getPortfolioSummary(String portfolioId) {
        Portfolio portfolio = portfolioRepository.findByPortfolioIdWithAssets(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "portfolioId", portfolioId));

        List<Asset> assets = portfolio.getAssets();
        
        // Calculate total assets value
        BigDecimal assetsValue = assets.stream()
                .map(Asset::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalValue = portfolio.getCashBalance().add(assetsValue);

        // Calculate total gain
        BigDecimal totalCost = assets.stream()
                .filter(a -> a.getPurchasePrice() != null)
                .map(a -> a.getQuantity().multiply(a.getPurchasePrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGain = assetsValue.subtract(totalCost);
        BigDecimal totalGainPercent = totalCost.compareTo(BigDecimal.ZERO) > 0
                ? totalGain.divide(totalCost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        // Get top holdings
        List<AssetDTO> topHoldings = assets.stream()
                .sorted((a, b) -> b.getTotalValue().compareTo(a.getTotalValue()))
                .limit(5)
                .map(a -> mapAssetToDTO(a, totalValue))
                .collect(Collectors.toList());

        // Calculate allocation by asset type
        Map<AssetType, BigDecimal> allocationByType = assets.stream()
                .collect(Collectors.groupingBy(
                        Asset::getAssetType,
                        Collectors.reducing(BigDecimal.ZERO, Asset::getTotalValue, BigDecimal::add)
                ));

        List<AllocationDTO> allocation = allocationByType.entrySet().stream()
                .map(e -> AllocationDTO.builder()
                        .assetType(e.getKey().name())
                        .value(e.getValue())
                        .percentage(totalValue.compareTo(BigDecimal.ZERO) > 0
                                ? e.getValue().divide(totalValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                                : BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());

        // Add cash to allocation
        if (portfolio.getCashBalance().compareTo(BigDecimal.ZERO) > 0) {
            allocation.add(AllocationDTO.builder()
                    .assetType("CASH")
                    .value(portfolio.getCashBalance())
                    .percentage(totalValue.compareTo(BigDecimal.ZERO) > 0
                            ? portfolio.getCashBalance().divide(totalValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                            : BigDecimal.ZERO)
                    .build());
        }

        long transactionCount = transactionRepository.countByPortfolioId(portfolio.getId());

        return PortfolioSummaryDTO.builder()
                .portfolioId(portfolio.getPortfolioId())
                .portfolioName(portfolio.getPortfolioName())
                .baseCurrency(portfolio.getBaseCurrency())
                .totalValue(totalValue)
                .cashBalance(portfolio.getCashBalance())
                .assetsValue(assetsValue)
                .totalGain(totalGain)
                .totalGainPercent(totalGainPercent)
                .assetCount(assets.size())
                .transactionCount((int) transactionCount)
                .topHoldings(topHoldings)
                .allocation(allocation)
                .build();
    }

    @Transactional
    public PortfolioDTO updatePortfolio(String portfolioId, PortfolioDTO dto) {
        Portfolio portfolio = portfolioRepository.findByPortfolioId(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "portfolioId", portfolioId));

        if (dto.getPortfolioName() != null) {
            portfolio.setPortfolioName(dto.getPortfolioName());
        }
        if (dto.getDescription() != null) {
            portfolio.setDescription(dto.getDescription());
        }
        if (dto.getCashBalance() != null) {
            portfolio.setCashBalance(dto.getCashBalance());
        }

        Portfolio updated = portfolioRepository.save(portfolio);
        log.info("Portfolio updated: {}", portfolioId);
        
        return mapToDTO(updated);
    }

    @Transactional
    public void deletePortfolio(String portfolioId) {
        Portfolio portfolio = portfolioRepository.findByPortfolioId(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "portfolioId", portfolioId));
        
        portfolioRepository.delete(portfolio);
        log.info("Portfolio deleted: {}", portfolioId);
    }

    @Transactional(readOnly = true)
    public Page<PortfolioDTO> searchPortfolios(String name, Pageable pageable) {
        return portfolioRepository.searchByName(name, pageable)
                .map(this::mapToDTO);
    }

    // Helper methods
    private PortfolioDTO mapToDTO(Portfolio portfolio) {
        List<Asset> assets = assetRepository.findByPortfolioId(portfolio.getId());
        
        BigDecimal assetsValue = assets.stream()
                .map(Asset::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PortfolioDTO.builder()
                .portfolioId(portfolio.getPortfolioId())
                .portfolioName(portfolio.getPortfolioName())
                .description(portfolio.getDescription())
                .baseCurrency(portfolio.getBaseCurrency())
                .totalValue(portfolio.getCashBalance().add(assetsValue))
                .cashBalance(portfolio.getCashBalance())
                .assetsValue(assetsValue)
                .assetCount(assets.size())
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt())
                .build();
    }

    private AssetDTO mapAssetToDTO(Asset asset, BigDecimal portfolioTotalValue) {
        BigDecimal allocation = portfolioTotalValue.compareTo(BigDecimal.ZERO) > 0
                ? asset.getTotalValue().divide(portfolioTotalValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return AssetDTO.builder()
                .assetId(asset.getAssetId())
                .portfolioId(asset.getPortfolio().getPortfolioId())
                .ticker(asset.getTicker())
                .assetName(asset.getAssetName())
                .assetType(asset.getAssetType().name())
                .quantity(asset.getQuantity())
                .purchasePrice(asset.getPurchasePrice())
                .currentPrice(asset.getCurrentPrice())
                .totalValue(asset.getTotalValue())
                .gainLoss(asset.getGainLoss())
                .gainLossPercentage(asset.getGainLossPercentage())
                .allocation(allocation)
                .purchaseDate(asset.getPurchaseDate())
                .notes(asset.getNotes())
                .createdAt(asset.getCreatedAt())
                .updatedAt(asset.getUpdatedAt())
                .build();
    }
}
