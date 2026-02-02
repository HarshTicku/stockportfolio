package com.portfoliomanager.service;

import com.portfoliomanager.dto.MarketDataDTO;
import com.portfoliomanager.entity.MarketData;
import com.portfoliomanager.exception.ResourceNotFoundException;
import com.portfoliomanager.repository.MarketDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketDataService {

    private final MarketDataRepository marketDataRepository;

    @Transactional
    public MarketDataDTO saveMarketData(MarketDataDTO dto) {
        // Check if data already exists for this ticker and date
        if (marketDataRepository.existsByTickerAndDate(dto.getTicker(), dto.getDate())) {
            log.debug("Market data already exists for {} on {}", dto.getTicker(), dto.getDate());
            return dto;
        }

        MarketData marketData = MarketData.builder()
                .ticker(dto.getTicker().toUpperCase())
                .date(dto.getDate())
                .openPrice(dto.getOpenPrice())
                .highPrice(dto.getHighPrice())
                .lowPrice(dto.getLowPrice())
                .closePrice(dto.getClosePrice())
                .volume(dto.getVolume())
                .build();

        MarketData saved = marketDataRepository.save(marketData);
        return mapToDTO(saved);
    }

    @Transactional
    public List<MarketDataDTO> saveMarketDataBatch(List<MarketDataDTO> dataList) {
        return dataList.stream()
                .map(this::saveMarketData)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MarketDataDTO getLatestPrice(String ticker) {
        MarketData marketData = marketDataRepository.findMostRecentByTicker(ticker.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("MarketData", "ticker", ticker));
        return mapToDTO(marketData);
    }

    @Transactional(readOnly = true)
    public BigDecimal getLatestClosePrice(String ticker) {
        return marketDataRepository.findMostRecentByTicker(ticker.toUpperCase())
                .map(MarketData::getClosePrice)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Page<MarketDataDTO> getMarketDataByTicker(String ticker, Pageable pageable) {
        return marketDataRepository.findByTicker(ticker.toUpperCase(), pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public List<MarketDataDTO> getMarketDataHistory(String ticker, LocalDate startDate, LocalDate endDate) {
        List<MarketData> data = marketDataRepository.findByTickerAndDateBetweenOrderByDateAsc(
                ticker.toUpperCase(), startDate, endDate);
        
        return data.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getAllTickers() {
        return marketDataRepository.findAllTickers();
    }

    @Transactional(readOnly = true)
    public List<MarketDataDTO> getLatestPrices(List<String> tickers) {
        return tickers.stream()
                .map(ticker -> marketDataRepository.findMostRecentByTicker(ticker.toUpperCase()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Helper method to map entity to DTO
    private MarketDataDTO mapToDTO(MarketData marketData) {
        BigDecimal change = marketData.getClosePrice().subtract(marketData.getOpenPrice());
        BigDecimal changePercent = marketData.getOpenPrice().compareTo(BigDecimal.ZERO) > 0
                ? change.divide(marketData.getOpenPrice(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return MarketDataDTO.builder()
                .id(marketData.getId())
                .ticker(marketData.getTicker())
                .date(marketData.getDate())
                .openPrice(marketData.getOpenPrice())
                .highPrice(marketData.getHighPrice())
                .lowPrice(marketData.getLowPrice())
                .closePrice(marketData.getClosePrice())
                .volume(marketData.getVolume())
                .change(change)
                .changePercent(changePercent)
                .build();
    }
}
