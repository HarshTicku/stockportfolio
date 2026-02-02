package com.portfoliomanager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_data",
       uniqueConstraints = @UniqueConstraint(columnNames = {"ticker", "date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
