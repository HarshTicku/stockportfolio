# Portfolio Manager - Implementation Examples

This document provides code examples and implementation patterns for the Portfolio Manager API.

---

## Table of Contents

1. [Java Entity Classes](#java-entity-classes)
2. [Repository Interfaces](#repository-interfaces)
3. [Service Layer](#service-layer)
4. [Controller Examples](#controller-examples)
5. [DTO Classes](#dto-classes)
6. [Exception Handling](#exception-handling)
7. [Application Configuration](#application-configuration)

---

## Java Entity Classes

### Portfolio Entity

```java
package com.portfoliomanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "portfolio_id", unique = true, nullable = false, length = 50)
    private String portfolioId;
    
    @Column(name = "portfolio_name", nullable = false)
    private String portfolioName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency = "USD";
    
    @Column(name = "total_value", precision = 18, scale = 2)
    private BigDecimal totalValue = BigDecimal.ZERO;
    
    @Column(name = "cash_balance", precision = 18, scale = 2)
    private BigDecimal cashBalance = BigDecimal.ZERO;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (portfolioId == null) {
            portfolioId = generatePortfolioId();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    private String generatePortfolioId() {
        return "PORT-" + String.format("%03d", id != null ? id : System.currentTimeMillis() % 1000);
    }
}
```

### Asset Entity

```java
package com.portfoliomanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "assets", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"portfolio_id", "ticker"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asset {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "asset_id", unique = true, nullable = false, length = 50)
    private String assetId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;
    
    @Column(name = "ticker", nullable = false, length = 20)
    private String ticker;
    
    @Column(name = "asset_name", nullable = false)
    private String assetName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false)
    private AssetType assetType;
    
    @Column(name = "quantity", precision = 18, scale = 8, nullable = false)
    private BigDecimal quantity = BigDecimal.ZERO;
    
    @Column(name = "purchase_price", precision = 18, scale = 2)
    private BigDecimal purchasePrice;
    
    @Column(name = "current_price", precision = 18, scale = 2)
    private BigDecimal currentPrice;
    
    @Column(name = "purchase_date")
    private LocalDate purchaseDate;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (assetId == null) {
            assetId = generateAssetId();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    private String generateAssetId() {
        return "ASSET-" + String.format("%03d", id != null ? id : System.currentTimeMillis() % 1000);
    }
    
    public enum AssetType {
        STOCK, BOND, CASH, ETF, CRYPTO, COMMODITY
    }
}
```

### MarketData Entity

```java
package com.portfoliomanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_data",
       uniqueConstraints = @UniqueConstraint(columnNames = {"ticker", "date"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "ticker", nullable = false, length = 20)
    private String ticker;
    
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @Column(name = "open_price", precision = 18, scale = 2, nullable = false)
    private BigDecimal openPrice;
    
    @Column(name = "high_price", precision = 18, scale = 2, nullable = false)
    private BigDecimal highPrice;
    
    @Column(name = "low_price", precision = 18, scale = 2, nullable = false)
    private BigDecimal lowPrice;
    
    @Column(name = "close_price", precision = 18, scale = 2, nullable = false)
    private BigDecimal closePrice;
    
    @Column(name = "volume", nullable = false)
    private Long volume;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

---

## Repository Interfaces

### PortfolioRepository

```java
package com.portfoliomanager.repository;

import com.portfoliomanager.entity.Portfolio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    
    Optional<Portfolio> findByPortfolioId(String portfolioId);
    
    boolean existsByPortfolioId(String portfolioId);
    
    Page<Portfolio> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT p FROM Portfolio p WHERE p.portfolioName LIKE %:name%")
    Page<Portfolio> searchByName(String name, Pageable pageable);
}
```

### AssetRepository

```java
package com.portfoliomanager.repository;

import com.portfoliomanager.entity.Asset;
import com.portfoliomanager.entity.Asset.AssetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    
    Optional<Asset> findByAssetId(String assetId);
    
    List<Asset> findByPortfolioId(Long portfolioId);
    
    Page<Asset> findByPortfolioId(Long portfolioId, Pageable pageable);
    
    Page<Asset> findByAssetType(AssetType assetType, Pageable pageable);
    
    Page<Asset> findByTicker(String ticker, Pageable pageable);
    
    Optional<Asset> findByPortfolioIdAndTicker(Long portfolioId, String ticker);
    
    boolean existsByPortfolioIdAndTicker(Long portfolioId, String ticker);
    
    @Query("SELECT a FROM Asset a WHERE a.portfolio.id = :portfolioId AND a.assetType = :assetType")
    List<Asset> findByPortfolioIdAndAssetType(Long portfolioId, AssetType assetType);
    
    @Query("SELECT COUNT(a) FROM Asset a WHERE a.portfolio.id = :portfolioId")
    long countByPortfolioId(Long portfolioId);
}
```

### MarketDataRepository

```java
package com.portfoliomanager.repository;

import com.portfoliomanager.entity.MarketData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MarketDataRepository extends JpaRepository<MarketData, Long> {
    
    Page<MarketData> findByTicker(String ticker, Pageable pageable);
    
    Page<MarketData> findByTickerAndDateBetween(
        String ticker, 
        LocalDate startDate, 
        LocalDate endDate, 
        Pageable pageable
    );
    
    @Query("SELECT m FROM MarketData m WHERE m.ticker = :ticker ORDER BY m.date DESC LIMIT 1")
    Optional<MarketData> findLatestByTicker(String ticker);
    
    @Query("SELECT DISTINCT m.ticker FROM MarketData m ORDER BY m.ticker")
    List<String> findAllTickers();
    
    @Query("SELECT m FROM MarketData m WHERE m.ticker = :ticker AND m.date = " +
           "(SELECT MAX(m2.date) FROM MarketData m2 WHERE m2.ticker = :ticker)")
    Optional<MarketData> findMostRecentByTicker(String ticker);
}
```

---

## Service Layer

### PortfolioService

```java
package com.portfoliomanager.service;

import com.portfoliomanager.dto.PortfolioDTO;
import com.portfoliomanager.dto.PortfolioSummaryDTO;
import com.portfoliomanager.entity.Portfolio;
import com.portfoliomanager.exception.ResourceNotFoundException;
import com.portfoliomanager.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PortfolioService {
    
    private final PortfolioRepository portfolioRepository;
    
    @Transactional
    public PortfolioDTO createPortfolio(PortfolioDTO dto) {
        Portfolio portfolio = new Portfolio();
        portfolio.setPortfolioName(dto.getPortfolioName());
        portfolio.setDescription(dto.getDescription());
        portfolio.setBaseCurrency(dto.getBaseCurrency());
        portfolio.setCashBalance(dto.getCashBalance());
        portfolio.setTotalValue(dto.getCashBalance());
        
        Portfolio saved = portfolioRepository.save(portfolio);
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
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Portfolio not found with ID: " + portfolioId));
        return mapToDTO(portfolio);
    }
    
    @Transactional
    public PortfolioDTO updatePortfolio(String portfolioId, PortfolioDTO dto) {
        Portfolio portfolio = portfolioRepository.findByPortfolioId(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Portfolio not found with ID: " + portfolioId));
        
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
        return mapToDTO(updated);
    }
    
    @Transactional
    public void deletePortfolio(String portfolioId) {
        Portfolio portfolio = portfolioRepository.findByPortfolioId(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Portfolio not found with ID: " + portfolioId));
        portfolioRepository.delete(portfolio);
    }
    
    private PortfolioDTO mapToDTO(Portfolio portfolio) {
        PortfolioDTO dto = new PortfolioDTO();
        dto.setPortfolioId(portfolio.getPortfolioId());
        dto.setPortfolioName(portfolio.getPortfolioName());
        dto.setDescription(portfolio.getDescription());
        dto.setBaseCurrency(portfolio.getBaseCurrency());
        dto.setTotalValue(portfolio.getTotalValue());
        dto.setCashBalance(portfolio.getCashBalance());
        dto.setCreatedAt(portfolio.getCreatedAt());
        dto.setUpdatedAt(portfolio.getUpdatedAt());
        return dto;
    }
}
```

### AssetService

```java
package com.portfoliomanager.service;

import com.portfoliomanager.dto.AssetDTO;
import com.portfoliomanager.entity.Asset;
import com.portfoliomanager.entity.Portfolio;
import com.portfoliomanager.exception.ResourceNotFoundException;
import com.portfoliomanager.exception.DuplicateResourceException;
import com.portfoliomanager.repository.AssetRepository;
import com.portfoliomanager.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AssetService {
    
    private final AssetRepository assetRepository;
    private final PortfolioRepository portfolioRepository;
    private final MarketDataService marketDataService;
    
    @Transactional
    public AssetDTO createAsset(AssetDTO dto) {
        Portfolio portfolio = portfolioRepository.findByPortfolioId(dto.getPortfolioId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Portfolio not found with ID: " + dto.getPortfolioId()));
        
        if (assetRepository.existsByPortfolioIdAndTicker(portfolio.getId(), dto.getTicker())) {
            throw new DuplicateResourceException(
                "Asset with ticker " + dto.getTicker() + 
                " already exists in portfolio " + dto.getPortfolioId());
        }
        
        Asset asset = new Asset();
        asset.setPortfolio(portfolio);
        asset.setTicker(dto.getTicker());
        asset.setAssetName(dto.getAssetName());
        asset.setAssetType(Asset.AssetType.valueOf(dto.getAssetType()));
        asset.setQuantity(dto.getQuantity());
        asset.setPurchasePrice(dto.getPurchasePrice());
        asset.setPurchaseDate(dto.getPurchaseDate());
        asset.setNotes(dto.getNotes());
        
        // Fetch current price from market data
        BigDecimal currentPrice = marketDataService.getLatestPrice(dto.getTicker());
        asset.setCurrentPrice(currentPrice);
        
        Asset saved = assetRepository.save(asset);
        return mapToDTO(saved);
    }
    
    @Transactional(readOnly = true)
    public Page<AssetDTO> getAllAssets(Pageable pageable) {
        return assetRepository.findAll(pageable).map(this::mapToDTO);
    }
    
    @Transactional(readOnly = true)
    public AssetDTO getAssetById(String assetId) {
        Asset asset = assetRepository.findByAssetId(assetId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Asset not found with ID: " + assetId));
        return mapToDTO(asset);
    }
    
    @Transactional
    public AssetDTO updateAsset(String assetId, AssetDTO dto) {
        Asset asset = assetRepository.findByAssetId(assetId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Asset not found with ID: " + assetId));
        
        if (dto.getQuantity() != null) {
            asset.setQuantity(dto.getQuantity());
        }
        if (dto.getPurchasePrice() != null) {
            asset.setPurchasePrice(dto.getPurchasePrice());
        }
        if (dto.getNotes() != null) {
            asset.setNotes(dto.getNotes());
        }
        
        // Update current price
        BigDecimal currentPrice = marketDataService.getLatestPrice(asset.getTicker());
        asset.setCurrentPrice(currentPrice);
        
        Asset updated = assetRepository.save(asset);
        return mapToDTO(updated);
    }
    
    @Transactional
    public void deleteAsset(String assetId) {
        Asset asset = assetRepository.findByAssetId(assetId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Asset not found with ID: " + assetId));
        assetRepository.delete(asset);
    }
    
    private AssetDTO mapToDTO(Asset asset) {
        AssetDTO dto = new AssetDTO();
        dto.setAssetId(asset.getAssetId());
        dto.setPortfolioId(asset.getPortfolio().getPortfolioId());
        dto.setTicker(asset.getTicker());
        dto.setAssetName(asset.getAssetName());
        dto.setAssetType(asset.getAssetType().name());
        dto.setQuantity(asset.getQuantity());
        dto.setPurchasePrice(asset.getPurchasePrice());
        dto.setCurrentPrice(asset.getCurrentPrice());
        dto.setPurchaseDate(asset.getPurchaseDate());
        dto.setNotes(asset.getNotes());
        dto.setCreatedAt(asset.getCreatedAt());
        dto.setUpdatedAt(asset.getUpdatedAt());
        
        // Calculate gain/loss
        if (asset.getPurchasePrice() != null && asset.getCurrentPrice() != null) {
            BigDecimal totalValue = asset.getQuantity().multiply(asset.getCurrentPrice());
            BigDecimal totalCost = asset.getQuantity().multiply(asset.getPurchasePrice());
            BigDecimal gainLoss = totalValue.subtract(totalCost);
            BigDecimal gainLossPercentage = gainLoss.divide(totalCost, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            
            dto.setTotalValue(totalValue);
            dto.setGainLoss(gainLoss);
            dto.setGainLossPercentage(gainLossPercentage);
        }
        
        return dto;
    }
}
```

---

## Controller Examples

### PortfolioController

```java
package com.portfoliomanager.controller;

import com.portfoliomanager.dto.PortfolioDTO;
import com.portfoliomanager.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "Create a new portfolio")
    public ResponseEntity<PortfolioDTO> createPortfolio(@Valid @RequestBody PortfolioDTO dto) {
        PortfolioDTO created = portfolioService.createPortfolio(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping
    @Operation(summary = "Get all portfolios")
    public ResponseEntity<Page<PortfolioDTO>> getAllPortfolios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PortfolioDTO> portfolios = portfolioService.getAllPortfolios(pageable);
        return ResponseEntity.ok(portfolios);
    }
    
    @GetMapping("/{portfolioId}")
    @Operation(summary = "Get portfolio by ID")
    public ResponseEntity<PortfolioDTO> getPortfolioById(@PathVariable String portfolioId) {
        PortfolioDTO portfolio = portfolioService.getPortfolioById(portfolioId);
        return ResponseEntity.ok(portfolio);
    }
    
    @PutMapping("/{portfolioId}")
    @Operation(summary = "Update portfolio")
    public ResponseEntity<PortfolioDTO> updatePortfolio(
            @PathVariable String portfolioId,
            @Valid @RequestBody PortfolioDTO dto) {
        PortfolioDTO updated = portfolioService.updatePortfolio(portfolioId, dto);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{portfolioId}")
    @Operation(summary = "Delete portfolio")
    public ResponseEntity<Void> deletePortfolio(@PathVariable String portfolioId) {
        portfolioService.deletePortfolio(portfolioId);
        return ResponseEntity.noContent().build();
    }
}
```

---

## DTO Classes

### PortfolioDTO

```java
package com.portfoliomanager.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PortfolioDTO {
    
    private String portfolioId;
    
    @NotBlank(message = "Portfolio name is required")
    @Size(max = 255, message = "Portfolio name must not exceed 255 characters")
    private String portfolioName;
    
    private String description;
    
    @NotBlank(message = "Base currency is required")
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    private String baseCurrency = "USD";
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Total value must be non-negative")
    private BigDecimal totalValue;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Cash balance must be non-negative")
    private BigDecimal cashBalance;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### AssetDTO

```java
package com.portfoliomanager.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AssetDTO {
    
    private String assetId;
    
    @NotBlank(message = "Portfolio ID is required")
    private String portfolioId;
    
    @NotBlank(message = "Ticker is required")
    @Size(max = 20, message = "Ticker must not exceed 20 characters")
    private String ticker;
    
    @NotBlank(message = "Asset name is required")
    private String assetName;
    
    @NotBlank(message = "Asset type is required")
    private String assetType;
    
    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than 0")
    private BigDecimal quantity;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Purchase price must be greater than 0")
    private BigDecimal purchasePrice;
    
    private BigDecimal currentPrice;
    private BigDecimal totalValue;
    private BigDecimal gainLoss;
    private BigDecimal gainLossPercentage;
    
    private LocalDate purchaseDate;
    private String notes;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

## Exception Handling

### Custom Exceptions

```java
package com.portfoliomanager.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
```

### Global Exception Handler

```java
package com.portfoliomanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value(),
            "Conflict",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        ValidationErrorResponse response = new ValidationErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            errors,
            request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
```

---

## Application Configuration

### application.properties

```properties
# Application Name
spring.application.name=portfolio-manager

# Server Configuration
server.port=8080
server.servlet.context-path=/

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/portfolio_manager?useSSL=false&serverTimezone=UTC
spring.datasource.username=portfolio_user
spring.datasource.password=Portfolio@123
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.use_sql_comments=true

# Connection Pool
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000

# Logging
logging.level.root=INFO
logging.level.com.portfoliomanager=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# Swagger/OpenAPI
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true

# Jackson Configuration
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.time-zone=UTC
```

### pom.xml Dependencies

```xml
<dependencies>
    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Boot Starter Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- Spring Boot Starter Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- MySQL Connector -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- Springdoc OpenAPI (Swagger) -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.2.0</version>
    </dependency>
    
    <!-- Spring Boot Starter Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

**Last Updated**: February 2, 2026  
**Version**: 1.0.0
