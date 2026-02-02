package com.portfoliomanager.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioSummaryDTO {

    private String portfolioId;
    private String portfolioName;
    private String baseCurrency;
    
    // Values
    private BigDecimal totalValue;
    private BigDecimal cashBalance;
    private BigDecimal assetsValue;
    
    // Performance
    private BigDecimal dailyChange;
    private BigDecimal dailyChangePercent;
    private BigDecimal totalGain;
    private BigDecimal totalGainPercent;
    
    // Counts
    private Integer assetCount;
    private Integer transactionCount;
    
    // Holdings breakdown
    private List<AssetDTO> topHoldings;
    private List<AllocationDTO> allocation;
}
