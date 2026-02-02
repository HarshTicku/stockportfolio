package com.portfoliomanager.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    
    // Calculated fields
    private BigDecimal totalValue;
    private BigDecimal gainLoss;
    private BigDecimal gainLossPercentage;
    private BigDecimal allocation; // Percentage of portfolio

    private LocalDate purchaseDate;
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
