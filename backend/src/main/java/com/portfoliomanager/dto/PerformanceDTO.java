package com.portfoliomanager.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceDTO {

    private String portfolioId;
    private String period; // 1D, 1W, 1M, 3M, 1Y, ALL
    
    private BigDecimal startValue;
    private BigDecimal endValue;
    private BigDecimal totalReturn;
    private BigDecimal totalReturnPercent;
    
    private BigDecimal highValue;
    private BigDecimal lowValue;
    
    private List<PerformanceDataPoint> dataPoints;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PerformanceDataPoint {
        private LocalDate date;
        private BigDecimal value;
        private BigDecimal dailyReturn;
    }
}
