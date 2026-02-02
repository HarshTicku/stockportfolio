package com.portfoliomanager.controller;

import com.portfoliomanager.dto.AssetDTO;
import com.portfoliomanager.service.AssetService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
@Tag(name = "Asset", description = "Asset management APIs")
public class AssetController {

    private final AssetService assetService;

    @PostMapping
    @Operation(summary = "Create a new asset", description = "Adds a new asset to a portfolio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Asset created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Portfolio not found"),
            @ApiResponse(responseCode = "409", description = "Asset already exists in portfolio")
    })
    public ResponseEntity<AssetDTO> createAsset(@Valid @RequestBody AssetDTO dto) {
        AssetDTO created = assetService.createAsset(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Get all assets", description = "Retrieves a paginated list of all assets")
    public ResponseEntity<Page<AssetDTO>> getAllAssets(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AssetDTO> assets = assetService.getAllAssets(pageable);
        return ResponseEntity.ok(assets);
    }

    @GetMapping("/{assetId}")
    @Operation(summary = "Get asset by ID", description = "Retrieves a specific asset by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Asset found"),
            @ApiResponse(responseCode = "404", description = "Asset not found")
    })
    public ResponseEntity<AssetDTO> getAssetById(
            @Parameter(description = "Asset ID") @PathVariable String assetId) {
        AssetDTO asset = assetService.getAssetById(assetId);
        return ResponseEntity.ok(asset);
    }

    @GetMapping("/portfolio/{portfolioId}")
    @Operation(summary = "Get assets by portfolio", description = "Retrieves all assets in a portfolio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assets retrieved"),
            @ApiResponse(responseCode = "404", description = "Portfolio not found")
    })
    public ResponseEntity<List<AssetDTO>> getAssetsByPortfolio(
            @Parameter(description = "Portfolio ID") @PathVariable String portfolioId) {
        List<AssetDTO> assets = assetService.getAssetsByPortfolio(portfolioId);
        return ResponseEntity.ok(assets);
    }

    @GetMapping("/type/{assetType}")
    @Operation(summary = "Get assets by type", description = "Retrieves assets filtered by type (STOCK, BOND, ETF, etc.)")
    public ResponseEntity<Page<AssetDTO>> getAssetsByType(
            @Parameter(description = "Asset type") @PathVariable String assetType,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AssetDTO> assets = assetService.getAssetsByType(assetType, pageable);
        return ResponseEntity.ok(assets);
    }

    @PutMapping("/{assetId}")
    @Operation(summary = "Update asset", description = "Updates an existing asset")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Asset updated"),
            @ApiResponse(responseCode = "404", description = "Asset not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<AssetDTO> updateAsset(
            @Parameter(description = "Asset ID") @PathVariable String assetId,
            @RequestBody AssetDTO dto) {
        AssetDTO updated = assetService.updateAsset(assetId, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{assetId}")
    @Operation(summary = "Delete asset", description = "Removes an asset from the portfolio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Asset deleted"),
            @ApiResponse(responseCode = "404", description = "Asset not found")
    })
    public ResponseEntity<Void> deleteAsset(
            @Parameter(description = "Asset ID") @PathVariable String assetId) {
        assetService.deleteAsset(assetId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/update-prices")
    @Operation(summary = "Update all asset prices", description = "Updates current prices for all assets from market data")
    public ResponseEntity<Void> updateAssetPrices() {
        assetService.updateAssetPrices();
        return ResponseEntity.ok().build();
    }
}
