package com.portfoliomanager.config;

import com.portfoliomanager.entity.*;
import com.portfoliomanager.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    private final PortfolioRepository portfolioRepository;
    private final AssetRepository assetRepository;
    private final MarketDataRepository marketDataRepository;
    private final TransactionRepository transactionRepository;

    @Bean
    @Profile({"h2", "mysql"}) // Run in both H2 and MySQL modes
    public CommandLineRunner loadSampleData() {
        return args -> {
            log.info("Loading sample data...");

            // Create sample portfolios
            Portfolio growthPortfolio = createPortfolio("Growth Portfolio", 
                    "Aggressive growth stocks focused on technology", 
                    new BigDecimal("15000.00"));
            
            Portfolio dividendPortfolio = createPortfolio("Dividend Portfolio", 
                    "Stable dividend-paying stocks for passive income", 
                    new BigDecimal("25000.00"));

            // Create sample assets for Growth Portfolio
            createAsset(growthPortfolio, "AAPL", "Apple Inc.", AssetType.STOCK, 
                    new BigDecimal("100"), new BigDecimal("150.00"), new BigDecimal("175.50"));
            
            createAsset(growthPortfolio, "GOOG", "Alphabet Inc.", AssetType.STOCK, 
                    new BigDecimal("50"), new BigDecimal("250.00"), new BigDecimal("320.50"));
            
            createAsset(growthPortfolio, "MSFT", "Microsoft Corporation", AssetType.STOCK, 
                    new BigDecimal("75"), new BigDecimal("300.00"), new BigDecimal("385.20"));
            
            createAsset(growthPortfolio, "AMZN", "Amazon.com Inc.", AssetType.STOCK, 
                    new BigDecimal("40"), new BigDecimal("160.00"), new BigDecimal("185.75"));

            // Create sample assets for Dividend Portfolio
            createAsset(dividendPortfolio, "AAPL", "Apple Inc.", AssetType.STOCK, 
                    new BigDecimal("200"), new BigDecimal("145.00"), new BigDecimal("175.50"));
            
            createAsset(dividendPortfolio, "MSFT", "Microsoft Corporation", AssetType.STOCK, 
                    new BigDecimal("150"), new BigDecimal("295.00"), new BigDecimal("385.20"));
            
            createAsset(dividendPortfolio, "JNJ", "Johnson & Johnson", AssetType.STOCK, 
                    new BigDecimal("100"), new BigDecimal("160.00"), new BigDecimal("158.50"));

            // Create sample transactions
            createTransaction(growthPortfolio, "AAPL", TransactionType.BUY, 
                    new BigDecimal("100"), new BigDecimal("150.00"));
            
            createTransaction(growthPortfolio, "GOOG", TransactionType.BUY, 
                    new BigDecimal("50"), new BigDecimal("250.00"));
            
            createTransaction(growthPortfolio, null, TransactionType.DEPOSIT, 
                    null, new BigDecimal("15000.00"));

            // Load market data from CSV if available
            loadMarketDataFromCSV();

            // If no CSV data, create sample market data
            if (marketDataRepository.count() == 0) {
                createSampleMarketData();
            }

            log.info("Sample data loaded successfully!");
            log.info("Created {} portfolios", portfolioRepository.count());
            log.info("Created {} assets", assetRepository.count());
            log.info("Created {} transactions", transactionRepository.count());
            log.info("Loaded {} market data records", marketDataRepository.count());
        };
    }

    private Portfolio createPortfolio(String name, String description, BigDecimal cashBalance) {
        Portfolio portfolio = Portfolio.builder()
                .portfolioName(name)
                .description(description)
                .baseCurrency("USD")
                .cashBalance(cashBalance)
                .totalValue(cashBalance)
                .build();
        return portfolioRepository.save(portfolio);
    }

    private Asset createAsset(Portfolio portfolio, String ticker, String name, 
                              AssetType type, BigDecimal quantity, 
                              BigDecimal purchasePrice, BigDecimal currentPrice) {
        Asset asset = Asset.builder()
                .portfolio(portfolio)
                .ticker(ticker)
                .assetName(name)
                .assetType(type)
                .quantity(quantity)
                .purchasePrice(purchasePrice)
                .currentPrice(currentPrice)
                .purchaseDate(LocalDate.now().minusMonths(6))
                .build();
        return assetRepository.save(asset);
    }

    private void createTransaction(Portfolio portfolio, String ticker, 
                                   TransactionType type, BigDecimal quantity, BigDecimal amount) {
        Transaction transaction = Transaction.builder()
                .portfolio(portfolio)
                .ticker(ticker)
                .transactionType(type)
                .quantity(quantity)
                .price(quantity != null && amount != null ? amount.divide(quantity, 2, java.math.RoundingMode.HALF_UP) : null)
                .amount(amount)
                .currency("USD")
                .status(TransactionStatus.COMPLETED)
                .build();
        transactionRepository.save(transaction);
    }

    private void loadMarketDataFromCSV() {
        try {
            ClassPathResource resource = new ClassPathResource("stock_dataset.csv");
            if (!resource.exists()) {
                log.info("No stock_dataset.csv found in resources, skipping CSV load");
                return;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            int count = 0;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                String line;
                boolean isHeader = true;
                
                while ((line = reader.readLine()) != null) {
                    if (isHeader) {
                        isHeader = false;
                        continue;
                    }

                    String[] parts = line.split(",");
                    if (parts.length >= 7) {
                        try {
                            MarketData data = MarketData.builder()
                                    .date(LocalDate.parse(parts[0], formatter))
                                    .openPrice(new BigDecimal(parts[1]))
                                    .highPrice(new BigDecimal(parts[2]))
                                    .lowPrice(new BigDecimal(parts[3]))
                                    .closePrice(new BigDecimal(parts[4]))
                                    .volume(Long.parseLong(parts[5].replace(".0", "")))
                                    .ticker(parts[6].trim())
                                    .build();
                            
                            if (!marketDataRepository.existsByTickerAndDate(data.getTicker(), data.getDate())) {
                                marketDataRepository.save(data);
                                count++;
                            }
                        } catch (Exception e) {
                            log.warn("Failed to parse line: {}", line);
                        }
                    }
                }
            }

            log.info("Loaded {} market data records from CSV", count);
        } catch (Exception e) {
            log.warn("Could not load market data from CSV: {}", e.getMessage());
        }
    }

    private void createSampleMarketData() {
        log.info("Creating sample market data...");
        
        String[] tickers = {"AAPL", "GOOG", "MSFT", "AMZN", "META", "NFLX"};
        BigDecimal[] basePrices = {
                new BigDecimal("175.50"), 
                new BigDecimal("320.50"), 
                new BigDecimal("385.20"), 
                new BigDecimal("185.75"), 
                new BigDecimal("325.80"), 
                new BigDecimal("485.30")
        };

        LocalDate today = LocalDate.now();
        
        for (int i = 0; i < tickers.length; i++) {
            BigDecimal basePrice = basePrices[i];
            
            // Create 30 days of historical data
            for (int day = 30; day >= 0; day--) {
                LocalDate date = today.minusDays(day);
                
                // Skip weekends
                if (date.getDayOfWeek().getValue() > 5) continue;

                // Random price variation (-3% to +3%)
                double variation = (Math.random() - 0.5) * 0.06;
                BigDecimal closePrice = basePrice.multiply(BigDecimal.valueOf(1 + variation));
                BigDecimal openPrice = closePrice.multiply(BigDecimal.valueOf(1 + (Math.random() - 0.5) * 0.02));
                BigDecimal highPrice = closePrice.max(openPrice).multiply(BigDecimal.valueOf(1 + Math.random() * 0.01));
                BigDecimal lowPrice = closePrice.min(openPrice).multiply(BigDecimal.valueOf(1 - Math.random() * 0.01));

                MarketData data = MarketData.builder()
                        .ticker(tickers[i])
                        .date(date)
                        .openPrice(openPrice.setScale(2, java.math.RoundingMode.HALF_UP))
                        .highPrice(highPrice.setScale(2, java.math.RoundingMode.HALF_UP))
                        .lowPrice(lowPrice.setScale(2, java.math.RoundingMode.HALF_UP))
                        .closePrice(closePrice.setScale(2, java.math.RoundingMode.HALF_UP))
                        .volume((long) (Math.random() * 50000000 + 10000000))
                        .build();

                marketDataRepository.save(data);
                
                // Update base price for next iteration
                basePrice = closePrice;
            }
        }
    }
}
