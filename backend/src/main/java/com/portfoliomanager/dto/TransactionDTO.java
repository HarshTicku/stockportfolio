package com.portfoliomanager.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDTO {

    private String transactionId;

    @NotBlank(message = "Portfolio ID is required")
    private String portfolioId;

    private String ticker;

    @NotBlank(message = "Transaction type is required")
    private String transactionType;

    private BigDecimal quantity;
    private BigDecimal price;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String currency = "USD";
    private String description;
    private String status;
    private LocalDateTime transactionDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
