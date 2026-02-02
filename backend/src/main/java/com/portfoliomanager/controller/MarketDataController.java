package com.portfoliomanager.controller;

import com.portfoliomanager.dto.MarketDataDTO;
import com.portfoliomanager.service.MarketDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/market-data")
@RequiredArgsConstructor
@Tag(name = "Market Data", description = "Market data and stock prices APIs")
public class MarketDataController {

    private final MarketDataService marketDataService;

    @GetMapping("/{ticker}/latest")
    @Operation(summary = "Get latest price", description = "Retrieves the most recent price data for a ticker")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Price data found"),
            @ApiResponse(responseCode = "404", description = "No data found for ticker")
    })
    public ResponseEntity<MarketDataDTO> getLatestPrice(
            @Parameter(description = "Stock ticker symbol") @PathVariable String ticker) {
        MarketDataDTO data = marketDataService.getLatestPrice(ticker);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/{ticker}")
    @Operation(summary = "Get market data by ticker", description = "Retrieves paginated historical data for a ticker")
    public ResponseEntity<Page<MarketDataDTO>> getMarketDataByTicker(
            @Parameter(description = "Stock ticker symbol") @PathVariable String ticker,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MarketDataDTO> data = marketDataService.getMarketDataByTicker(ticker, pageable);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/{ticker}/history")
    @Operation(summary = "Get price history", description = "Retrieves historical price data for a date range")
    public ResponseEntity<List<MarketDataDTO>> getMarketDataHistory(
            @Parameter(description = "Stock ticker symbol") @PathVariable String ticker,
            @Parameter(description = "Start date (YYYY-MM-DD)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<MarketDataDTO> data = marketDataService.getMarketDataHistory(ticker, startDate, endDate);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/tickers")
    @Operation(summary = "Get all tickers", description = "Retrieves list of all available ticker symbols")
    public ResponseEntity<List<String>> getAllTickers() {
        List<String> tickers = marketDataService.getAllTickers();
        return ResponseEntity.ok(tickers);
    }

    @GetMapping("/latest")
    @Operation(summary = "Get latest prices for multiple tickers", description = "Retrieves latest price for specified tickers")
    public ResponseEntity<List<MarketDataDTO>> getLatestPrices(
            @Parameter(description = "Comma-separated list of tickers") 
            @RequestParam List<String> tickers) {
        List<MarketDataDTO> data = marketDataService.getLatestPrices(tickers);
        return ResponseEntity.ok(data);
    }

    @PostMapping
    @Operation(summary = "Add market data", description = "Adds new market data entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Data added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<MarketDataDTO> addMarketData(@RequestBody MarketDataDTO dto) {
        MarketDataDTO saved = marketDataService.saveMarketData(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/batch")
    @Operation(summary = "Add market data batch", description = "Adds multiple market data entries")
    public ResponseEntity<List<MarketDataDTO>> addMarketDataBatch(@RequestBody List<MarketDataDTO> dataList) {
        List<MarketDataDTO> saved = marketDataService.saveMarketDataBatch(dataList);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
