package com.portfoliomanager.service;

import com.portfoliomanager.dto.AssetDTO;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetService {

    private final AssetRepository assetRepository;
    private final PortfolioRepository portfolioRepository;
    private final MarketDataRepository marketDataRepository;

    @Transactional
    public AssetDTO createAsset(AssetDTO dto) {
        log.info("Creating asset {} for portfolio {}", dto.getTicker(), dto.getPortfolioId());
        
        Portfolio portfolio = portfolioRepository.findByPortfolioId(dto.getPortfolioId())
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "portfolioId", dto.getPortfolioId()));

        // Check for duplicate ticker in portfolio
        if (assetRepository.existsByPortfolioIdAndTicker(portfolio.getId(), dto.getTicker())) {
            throw new DuplicateResourceException("Asset", "ticker", dto.getTicker() + " in portfolio " + dto.getPortfolioId());
        }

        AssetType assetType;
        try {
            assetType = AssetType.valueOf(dto.getAssetType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid asset type: " + dto.getAssetType());
        }

        Asset asset = Asset.builder()
                .portfolio(portfolio)
                .ticker(dto.getTicker().toUpperCase())
                .assetName(dto.getAssetName())
                .assetType(assetType)
                .quantity(dto.getQuantity())
                .purchasePrice(dto.getPurchasePrice())
                .purchaseDate(dto.getPurchaseDate())
                .notes(dto.getNotes())
                .build();

        // Try to get current price from market data
        marketDataRepository.findMostRecentByTicker(dto.getTicker().toUpperCase())
                .ifPresent(md -> asset.setCurrentPrice(md.getClosePrice()));

        // If no market data, use purchase price as current price
        if (asset.getCurrentPrice() == null && asset.getPurchasePrice() != null) {
            asset.setCurrentPrice(asset.getPurchasePrice());
        }

        Asset saved = assetRepository.save(asset);
        log.info("Asset created with ID: {}", saved.getAssetId());

        return mapToDTO(saved, calculatePortfolioValue(portfolio.getId()));
    }

    @Transactional(readOnly = true)
    public Page<AssetDTO> getAllAssets(Pageable pageable) {
        return assetRepository.findAll(pageable)
                .map(asset -> mapToDTO(asset, calculatePortfolioValue(asset.getPortfolio().getId())));
    }

    @Transactional(readOnly = true)
    public AssetDTO getAssetById(String assetId) {
        Asset asset = assetRepository.findByAssetId(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "assetId", assetId));
        return mapToDTO(asset, calculatePortfolioValue(asset.getPortfolio().getId()));
    }

    @Transactional(readOnly = true)
    public List<AssetDTO> getAssetsByPortfolio(String portfolioId) {
        Portfolio portfolio = portfolioRepository.findByPortfolioId(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "portfolioId", portfolioId));

        BigDecimal portfolioValue = calculatePortfolioValue(portfolio.getId());
        
        return assetRepository.findByPortfolioId(portfolio.getId()).stream()
                .map(asset -> mapToDTO(asset, portfolioValue))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AssetDTO> getAssetsByType(String assetType, Pageable pageable) {
        AssetType type;
        try {
            type = AssetType.valueOf(assetType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid asset type: " + assetType);
        }
        
        return assetRepository.findByAssetType(type, pageable)
                .map(asset -> mapToDTO(asset, calculatePortfolioValue(asset.getPortfolio().getId())));
    }

    @Transactional
    public AssetDTO updateAsset(String assetId, AssetDTO dto) {
        Asset asset = assetRepository.findByAssetId(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "assetId", assetId));

        if (dto.getQuantity() != null) {
            asset.setQuantity(dto.getQuantity());
        }
        if (dto.getPurchasePrice() != null) {
            asset.setPurchasePrice(dto.getPurchasePrice());
        }
        if (dto.getCurrentPrice() != null) {
            asset.setCurrentPrice(dto.getCurrentPrice());
        }
        if (dto.getAssetName() != null) {
            asset.setAssetName(dto.getAssetName());
        }
        if (dto.getNotes() != null) {
            asset.setNotes(dto.getNotes());
        }
        if (dto.getPurchaseDate() != null) {
            asset.setPurchaseDate(dto.getPurchaseDate());
        }

        Asset updated = assetRepository.save(asset);
        log.info("Asset updated: {}", assetId);

        return mapToDTO(updated, calculatePortfolioValue(asset.getPortfolio().getId()));
    }

    @Transactional
    public void deleteAsset(String assetId) {
        Asset asset = assetRepository.findByAssetId(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "assetId", assetId));
        
        assetRepository.delete(asset);
        log.info("Asset deleted: {}", assetId);
    }

    @Transactional
    public void updateAssetPrices() {
        log.info("Updating asset prices from market data...");
        
        List<String> tickers = assetRepository.findAllDistinctTickers();
        
        for (String ticker : tickers) {
            marketDataRepository.findMostRecentByTicker(ticker)
                    .ifPresent(md -> {
                        List<Asset> assets = assetRepository.findByTicker(ticker);
                        assets.forEach(asset -> {
                            asset.setCurrentPrice(md.getClosePrice());
                            assetRepository.save(asset);
                        });
                        log.debug("Updated price for {} assets with ticker {}", assets.size(), ticker);
                    });
        }
        
        log.info("Asset price update completed");
    }

    // Helper methods
    private BigDecimal calculatePortfolioValue(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId).orElse(null);
        if (portfolio == null) return BigDecimal.ZERO;

        BigDecimal assetsValue = assetRepository.findByPortfolioId(portfolioId).stream()
                .map(Asset::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return portfolio.getCashBalance().add(assetsValue);
    }

    private AssetDTO mapToDTO(Asset asset, BigDecimal portfolioTotalValue) {
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
