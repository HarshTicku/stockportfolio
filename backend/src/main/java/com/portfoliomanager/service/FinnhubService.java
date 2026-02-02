package com.portfoliomanager.service;

import com.portfoliomanager.dto.StockQuoteDTO;
import com.portfoliomanager.dto.CompanyProfileDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinnhubService {

    private final RestTemplate restTemplate;

    @Value("${finnhub.api-key}")
    private String apiKey;

    @Value("${finnhub.base-url}")
    private String baseUrl;

    /**
     * Get real-time quote for a stock
     */
    public StockQuoteDTO getQuote(String symbol) {
        String url = String.format("%s/quote?symbol=%s&token=%s", baseUrl, symbol.toUpperCase(), apiKey);
        
        try {
            Map<String, Object> response = restTemplate.getForObject(url, HashMap.class);
            
            if (response != null && response.get("c") != null) {
                return StockQuoteDTO.builder()
                        .symbol(symbol.toUpperCase())
                        .currentPrice(toBigDecimal(response.get("c")))
                        .change(toBigDecimal(response.get("d")))
                        .changePercent(toBigDecimal(response.get("dp")))
                        .highPrice(toBigDecimal(response.get("h")))
                        .lowPrice(toBigDecimal(response.get("l")))
                        .openPrice(toBigDecimal(response.get("o")))
                        .previousClose(toBigDecimal(response.get("pc")))
                        .timestamp(System.currentTimeMillis())
                        .build();
            }
        } catch (Exception e) {
            log.error("Error fetching quote for {}: {}", symbol, e.getMessage());
        }
        
        return null;
    }

    /**
     * Get company profile
     */
    public CompanyProfileDTO getCompanyProfile(String symbol) {
        String url = String.format("%s/stock/profile2?symbol=%s&token=%s", baseUrl, symbol.toUpperCase(), apiKey);
        
        try {
            Map<String, Object> response = restTemplate.getForObject(url, HashMap.class);
            
            if (response != null && response.get("name") != null) {
                return CompanyProfileDTO.builder()
                        .symbol(symbol.toUpperCase())
                        .name((String) response.get("name"))
                        .country((String) response.get("country"))
                        .currency((String) response.get("currency"))
                        .exchange((String) response.get("exchange"))
                        .industry((String) response.get("finnhubIndustry"))
                        .logo((String) response.get("logo"))
                        .marketCap(toBigDecimal(response.get("marketCapitalization")))
                        .weburl((String) response.get("weburl"))
                        .build();
            }
        } catch (Exception e) {
            log.error("Error fetching company profile for {}: {}", symbol, e.getMessage());
        }
        
        return null;
    }

    /**
     * Get quotes for multiple symbols
     */
    public List<StockQuoteDTO> getQuotes(List<String> symbols) {
        return symbols.stream()
                .map(this::getQuote)
                .filter(quote -> quote != null)
                .collect(Collectors.toList());
    }

    /**
     * Search for stocks by query
     */
    public List<Map<String, Object>> searchStocks(String query) {
        String url = String.format("%s/search?q=%s&token=%s", baseUrl, query, apiKey);
        
        try {
            Map<String, Object> response = restTemplate.getForObject(url, HashMap.class);
            
            if (response != null && response.get("result") != null) {
                return (List<Map<String, Object>>) response.get("result");
            }
        } catch (Exception e) {
            log.error("Error searching stocks for {}: {}", query, e.getMessage());
        }
        
        return List.of();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return BigDecimal.ZERO;
    }
}
