package com.portfoliomanager.controller;

import com.portfoliomanager.dto.StockQuoteDTO;
import com.portfoliomanager.dto.CompanyProfileDTO;
import com.portfoliomanager.service.FinnhubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
@Tag(name = "Stocks", description = "Real-time stock data from Finnhub API")
public class StockController {

    private final FinnhubService finnhubService;

    @GetMapping("/{symbol}/quote")
    @Operation(summary = "Get real-time stock quote", description = "Fetches live price data from Finnhub")
    public ResponseEntity<StockQuoteDTO> getQuote(
            @Parameter(description = "Stock symbol (e.g., AAPL, GOOG)") @PathVariable String symbol) {
        StockQuoteDTO quote = finnhubService.getQuote(symbol);
        if (quote == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(quote);
    }

    @GetMapping("/{symbol}/profile")
    @Operation(summary = "Get company profile", description = "Fetches company information from Finnhub")
    public ResponseEntity<CompanyProfileDTO> getCompanyProfile(
            @Parameter(description = "Stock symbol") @PathVariable String symbol) {
        CompanyProfileDTO profile = finnhubService.getCompanyProfile(symbol);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/quotes")
    @Operation(summary = "Get quotes for multiple stocks", description = "Fetches live prices for multiple symbols")
    public ResponseEntity<List<StockQuoteDTO>> getQuotes(
            @Parameter(description = "Comma-separated list of symbols") @RequestParam List<String> symbols) {
        List<StockQuoteDTO> quotes = finnhubService.getQuotes(symbols);
        return ResponseEntity.ok(quotes);
    }

    @GetMapping("/search")
    @Operation(summary = "Search stocks", description = "Search for stocks by company name or symbol")
    public ResponseEntity<List<Map<String, Object>>> searchStocks(
            @Parameter(description = "Search query") @RequestParam String query) {
        List<Map<String, Object>> results = finnhubService.searchStocks(query);
        return ResponseEntity.ok(results);
    }
}
