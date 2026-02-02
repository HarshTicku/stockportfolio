package com.portfoliomanager.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyProfileDTO {
    private String symbol;
    private String name;
    private String country;
    private String currency;
    private String exchange;
    private String industry;
    private String logo;
    private BigDecimal marketCap;
    private String weburl;
}
