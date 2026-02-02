package com.portfoliomanager.service;

import com.portfoliomanager.dto.TransactionDTO;
import com.portfoliomanager.entity.*;
import com.portfoliomanager.exception.*;
import com.portfoliomanager.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final PortfolioRepository portfolioRepository;

    @Transactional
    public TransactionDTO createTransaction(TransactionDTO dto) {
        log.info("Creating transaction for portfolio {}", dto.getPortfolioId());
        
        Portfolio portfolio = portfolioRepository.findByPortfolioId(dto.getPortfolioId())
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "portfolioId", dto.getPortfolioId()));

        TransactionType transactionType;
        try {
            transactionType = TransactionType.valueOf(dto.getTransactionType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid transaction type: " + dto.getTransactionType());
        }

        Transaction transaction = Transaction.builder()
                .portfolio(portfolio)
                .ticker(dto.getTicker() != null ? dto.getTicker().toUpperCase() : null)
                .transactionType(transactionType)
                .quantity(dto.getQuantity())
                .price(dto.getPrice())
                .amount(dto.getAmount())
                .currency(dto.getCurrency() != null ? dto.getCurrency() : "USD")
                .description(dto.getDescription())
                .transactionDate(dto.getTransactionDate() != null ? dto.getTransactionDate() : LocalDateTime.now())
                .status(TransactionStatus.COMPLETED)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction created with ID: {}", saved.getTransactionId());

        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public Page<TransactionDTO> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public TransactionDTO getTransactionById(String transactionId) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "transactionId", transactionId));
        return mapToDTO(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByPortfolio(String portfolioId) {
        Portfolio portfolio = portfolioRepository.findByPortfolioId(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "portfolioId", portfolioId));

        return transactionRepository.findByPortfolioIdOrderByTransactionDateDesc(portfolio.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<TransactionDTO> getTransactionsByPortfolio(String portfolioId, Pageable pageable) {
        Portfolio portfolio = portfolioRepository.findByPortfolioId(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "portfolioId", portfolioId));

        return transactionRepository.findByPortfolioId(portfolio.getId(), pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByDateRange(String portfolioId, LocalDateTime startDate, LocalDateTime endDate) {
        Portfolio portfolio = portfolioRepository.findByPortfolioId(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "portfolioId", portfolioId));

        return transactionRepository.findByPortfolioIdAndDateRange(portfolio.getId(), startDate, endDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteTransaction(String transactionId) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "transactionId", transactionId));
        
        transactionRepository.delete(transaction);
        log.info("Transaction deleted: {}", transactionId);
    }

    // Helper method
    private TransactionDTO mapToDTO(Transaction transaction) {
        return TransactionDTO.builder()
                .transactionId(transaction.getTransactionId())
                .portfolioId(transaction.getPortfolio().getPortfolioId())
                .ticker(transaction.getTicker())
                .transactionType(transaction.getTransactionType().name())
                .quantity(transaction.getQuantity())
                .price(transaction.getPrice())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .description(transaction.getDescription())
                .status(transaction.getStatus().name())
                .transactionDate(transaction.getTransactionDate())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}
