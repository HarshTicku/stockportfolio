package com.portfoliomanager.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    
    // Calculated fields
    private BigDecimal assetsValue;
    private Integer assetCount;
}
