package com.portfoliomanager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "assets",
       uniqueConstraints = @UniqueConstraint(columnNames = {"portfolio_id", "ticker"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
            assetId = "ASSET-" + System.currentTimeMillis();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Calculate total value
    public BigDecimal getTotalValue() {
        if (quantity != null && currentPrice != null) {
            return quantity.multiply(currentPrice);
        }
        return BigDecimal.ZERO;
    }

    // Calculate gain/loss
    public BigDecimal getGainLoss() {
        if (quantity != null && currentPrice != null && purchasePrice != null) {
            BigDecimal currentValue = quantity.multiply(currentPrice);
            BigDecimal costBasis = quantity.multiply(purchasePrice);
            return currentValue.subtract(costBasis);
        }
        return BigDecimal.ZERO;
    }

    // Calculate gain/loss percentage
    public BigDecimal getGainLossPercentage() {
        if (purchasePrice != null && purchasePrice.compareTo(BigDecimal.ZERO) > 0 && currentPrice != null) {
            return currentPrice.subtract(purchasePrice)
                    .divide(purchasePrice, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO;
    }
}
