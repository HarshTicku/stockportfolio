package com.portfoliomanager.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllocationDTO {

    private String assetType;
    private String ticker;
    private String name;
    private BigDecimal value;
    private BigDecimal percentage;
}
