package com.portfoliomanager.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketDataDTO {

    private Long id;
    private String ticker;
    private LocalDate date;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal closePrice;
    private Long volume;
    
    // Calculated fields
    private BigDecimal change;
    private BigDecimal changePercent;
}
