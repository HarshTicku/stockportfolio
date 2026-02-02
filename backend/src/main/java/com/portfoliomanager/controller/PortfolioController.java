package com.portfoliomanager.controller;

import com.portfoliomanager.dto.PortfolioDTO;
import com.portfoliomanager.dto.PortfolioSummaryDTO;
import com.portfoliomanager.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/portfolios")
@RequiredArgsConstructor
@Tag(name = "Portfolio", description = "Portfolio management APIs")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @PostMapping
    @Operation(summary = "Create a new portfolio", description = "Creates a new investment portfolio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Portfolio created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<PortfolioDTO> createPortfolio(
            @Valid @RequestBody PortfolioDTO dto) {
        PortfolioDTO created = portfolioService.createPortfolio(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Get all portfolios", description = "Retrieves a paginated list of all portfolios")
    public ResponseEntity<Page<PortfolioDTO>> getAllPortfolios(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PortfolioDTO> portfolios = portfolioService.getAllPortfolios(pageable);
        return ResponseEntity.ok(portfolios);
    }

    @GetMapping("/{portfolioId}")
    @Operation(summary = "Get portfolio by ID", description = "Retrieves a specific portfolio by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Portfolio found"),
            @ApiResponse(responseCode = "404", description = "Portfolio not found")
    })
    public ResponseEntity<PortfolioDTO> getPortfolioById(
            @Parameter(description = "Portfolio ID") @PathVariable String portfolioId) {
        PortfolioDTO portfolio = portfolioService.getPortfolioById(portfolioId);
        return ResponseEntity.ok(portfolio);
    }

    @GetMapping("/{portfolioId}/summary")
    @Operation(summary = "Get portfolio summary", description = "Retrieves detailed summary including holdings and allocation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Summary retrieved"),
            @ApiResponse(responseCode = "404", description = "Portfolio not found")
    })
    public ResponseEntity<PortfolioSummaryDTO> getPortfolioSummary(
            @Parameter(description = "Portfolio ID") @PathVariable String portfolioId) {
        PortfolioSummaryDTO summary = portfolioService.getPortfolioSummary(portfolioId);
        return ResponseEntity.ok(summary);
    }

    @PutMapping("/{portfolioId}")
    @Operation(summary = "Update portfolio", description = "Updates an existing portfolio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Portfolio updated"),
            @ApiResponse(responseCode = "404", description = "Portfolio not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<PortfolioDTO> updatePortfolio(
            @Parameter(description = "Portfolio ID") @PathVariable String portfolioId,
            @Valid @RequestBody PortfolioDTO dto) {
        PortfolioDTO updated = portfolioService.updatePortfolio(portfolioId, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{portfolioId}")
    @Operation(summary = "Delete portfolio", description = "Deletes a portfolio and all its assets")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Portfolio deleted"),
            @ApiResponse(responseCode = "404", description = "Portfolio not found")
    })
    public ResponseEntity<Void> deletePortfolio(
            @Parameter(description = "Portfolio ID") @PathVariable String portfolioId) {
        portfolioService.deletePortfolio(portfolioId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search portfolios", description = "Search portfolios by name")
    public ResponseEntity<Page<PortfolioDTO>> searchPortfolios(
            @Parameter(description = "Search query") @RequestParam String query,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PortfolioDTO> portfolios = portfolioService.searchPortfolios(query, pageable);
        return ResponseEntity.ok(portfolios);
    }
}
