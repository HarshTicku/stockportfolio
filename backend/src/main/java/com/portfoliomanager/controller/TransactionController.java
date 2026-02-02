package com.portfoliomanager.controller;

import com.portfoliomanager.dto.TransactionDTO;
import com.portfoliomanager.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction", description = "Transaction management APIs")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Create a transaction", description = "Records a new transaction (BUY, SELL, DEPOSIT, etc.)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transaction created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Portfolio not found")
    })
    public ResponseEntity<TransactionDTO> createTransaction(@Valid @RequestBody TransactionDTO dto) {
        TransactionDTO created = transactionService.createTransaction(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Get all transactions", description = "Retrieves a paginated list of all transactions")
    public ResponseEntity<Page<TransactionDTO>> getAllTransactions(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionDTO> transactions = transactionService.getAllTransactions(pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{transactionId}")
    @Operation(summary = "Get transaction by ID", description = "Retrieves a specific transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction found"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<TransactionDTO> getTransactionById(
            @Parameter(description = "Transaction ID") @PathVariable String transactionId) {
        TransactionDTO transaction = transactionService.getTransactionById(transactionId);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/portfolio/{portfolioId}")
    @Operation(summary = "Get transactions by portfolio", description = "Retrieves all transactions for a portfolio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions retrieved"),
            @ApiResponse(responseCode = "404", description = "Portfolio not found")
    })
    public ResponseEntity<List<TransactionDTO>> getTransactionsByPortfolio(
            @Parameter(description = "Portfolio ID") @PathVariable String portfolioId) {
        List<TransactionDTO> transactions = transactionService.getTransactionsByPortfolio(portfolioId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/portfolio/{portfolioId}/paginated")
    @Operation(summary = "Get transactions by portfolio (paginated)", description = "Retrieves paginated transactions for a portfolio")
    public ResponseEntity<Page<TransactionDTO>> getTransactionsByPortfolioPaginated(
            @Parameter(description = "Portfolio ID") @PathVariable String portfolioId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionDTO> transactions = transactionService.getTransactionsByPortfolio(portfolioId, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/portfolio/{portfolioId}/range")
    @Operation(summary = "Get transactions by date range", description = "Retrieves transactions within a date range")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByDateRange(
            @Parameter(description = "Portfolio ID") @PathVariable String portfolioId,
            @Parameter(description = "Start date-time") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date-time") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<TransactionDTO> transactions = transactionService.getTransactionsByDateRange(portfolioId, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    @DeleteMapping("/{transactionId}")
    @Operation(summary = "Delete transaction", description = "Removes a transaction record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Transaction deleted"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<Void> deleteTransaction(
            @Parameter(description = "Transaction ID") @PathVariable String transactionId) {
        transactionService.deleteTransaction(transactionId);
        return ResponseEntity.noContent().build();
    }
}
