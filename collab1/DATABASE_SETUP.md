# Database Setup Guide

## Portfolio Manager Database Schema

This document provides SQL scripts to set up the Portfolio Manager database.

---

## 1. Create Database and User

```sql
-- Create the database
CREATE DATABASE IF NOT EXISTS portfolio_manager
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Create user and grant privileges
CREATE USER IF NOT EXISTS 'portfolio_user'@'localhost' IDENTIFIED BY 'Portfolio@123';
GRANT ALL PRIVILEGES ON portfolio_manager.* TO 'portfolio_user'@'localhost';
FLUSH PRIVILEGES;

-- Use the database
USE portfolio_manager;
```

---

## 2. Create Tables

### Portfolios Table

```sql
CREATE TABLE IF NOT EXISTS portfolios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id VARCHAR(50) UNIQUE NOT NULL,
    portfolio_name VARCHAR(255) NOT NULL,
    description TEXT,
    base_currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    total_value DECIMAL(18, 2) DEFAULT 0.00,
    cash_balance DECIMAL(18, 2) DEFAULT 0.00,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_portfolio_id (portfolio_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### Assets Table

```sql
CREATE TABLE IF NOT EXISTS assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_id VARCHAR(50) UNIQUE NOT NULL,
    portfolio_id BIGINT NOT NULL,
    ticker VARCHAR(20) NOT NULL,
    asset_name VARCHAR(255) NOT NULL,
    asset_type ENUM('STOCK', 'BOND', 'CASH', 'ETF', 'CRYPTO', 'COMMODITY') NOT NULL,
    quantity DECIMAL(18, 8) NOT NULL DEFAULT 0,
    purchase_price DECIMAL(18, 2),
    current_price DECIMAL(18, 2),
    purchase_date DATE,
    notes TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE,
    UNIQUE KEY unique_portfolio_ticker (portfolio_id, ticker),
    INDEX idx_portfolio_id (portfolio_id),
    INDEX idx_ticker (ticker),
    INDEX idx_asset_type (asset_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### Market Data Table

```sql
CREATE TABLE IF NOT EXISTS market_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticker VARCHAR(20) NOT NULL,
    date DATE NOT NULL,
    open_price DECIMAL(18, 2) NOT NULL,
    high_price DECIMAL(18, 2) NOT NULL,
    low_price DECIMAL(18, 2) NOT NULL,
    close_price DECIMAL(18, 2) NOT NULL,
    volume BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_ticker_date (ticker, date),
    INDEX idx_ticker (ticker),
    INDEX idx_date (date),
    INDEX idx_ticker_date (ticker, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 3. Insert Sample Data

### Sample Portfolios

```sql
INSERT INTO portfolios (portfolio_id, portfolio_name, description, base_currency, total_value, cash_balance)
VALUES
    ('PORT-001', 'Growth Portfolio', 'Aggressive growth stocks focused on technology sector', 'USD', 125000.00, 15000.00),
    ('PORT-002', 'Dividend Portfolio', 'Stable dividend-paying stocks for passive income', 'USD', 250000.00, 25000.00),
    ('PORT-003', 'Tech Portfolio', 'Technology sector focus with FAANG stocks', 'USD', 180000.00, 20000.00);
```

### Sample Assets

```sql
INSERT INTO assets (asset_id, portfolio_id, ticker, asset_name, asset_type, quantity, purchase_price, current_price, purchase_date, notes)
VALUES
    -- Portfolio 1 Assets
    ('ASSET-001', 1, 'AAPL', 'Apple Inc.', 'STOCK', 100, 150.00, 175.50, '2025-06-15', 'Long-term hold'),
    ('ASSET-002', 1, 'GOOG', 'Alphabet Inc.', 'STOCK', 50, 250.00, 320.50, '2025-07-20', 'Growth stock'),
    ('ASSET-003', 1, 'MSFT', 'Microsoft Corporation', 'STOCK', 75, 300.00, 385.20, '2025-08-10', 'Cloud computing leader'),
    
    -- Portfolio 2 Assets
    ('ASSET-004', 2, 'AAPL', 'Apple Inc.', 'STOCK', 200, 145.00, 175.50, '2025-05-01', 'Dividend stock'),
    ('ASSET-005', 2, 'MSFT', 'Microsoft Corporation', 'STOCK', 150, 295.00, 385.20, '2025-06-15', 'Stable dividend payer'),
    
    -- Portfolio 3 Assets
    ('ASSET-006', 3, 'GOOG', 'Alphabet Inc.', 'STOCK', 100, 240.00, 320.50, '2025-04-10', 'Tech giant'),
    ('ASSET-007', 3, 'AMZN', 'Amazon.com Inc.', 'STOCK', 80, 160.00, 185.75, '2025-05-20', 'E-commerce leader'),
    ('ASSET-008', 3, 'META', 'Meta Platforms Inc.', 'STOCK', 120, 280.00, 325.80, '2025-06-01', 'Social media'),
    ('ASSET-009', 3, 'NFLX', 'Netflix Inc.', 'STOCK', 60, 420.00, 485.30, '2025-07-15', 'Streaming leader');
```

### Sample Market Data (GOOG - First week of 2026)

```sql
INSERT INTO market_data (ticker, date, open_price, high_price, low_price, close_price, volume)
VALUES
    ('GOOG', '2026-01-02', 317.59, 322.91, 310.65, 315.32, 22043700),
    ('GOOG', '2026-01-05', 317.70, 319.25, 315.25, 317.32, 19934000),
    ('GOOG', '2026-01-06', 317.31, 321.56, 312.34, 314.55, 18989900),
    ('GOOG', '2026-01-07', 314.57, 326.46, 314.50, 322.43, 24681800),
    ('GOOG', '2026-01-08', 329.27, 330.54, 321.99, 326.01, 21789600),
    ('GOOG', '2026-01-09', 327.49, 331.48, 326.25, 329.14, 17917700),
    ('GOOG', '2026-01-12', 326.50, 334.44, 325.51, 332.73, 23893800);
```

---

## 4. Verify Installation

```sql
-- Check tables
SHOW TABLES;

-- Count records
SELECT 'Portfolios' as Table_Name, COUNT(*) as Record_Count FROM portfolios
UNION ALL
SELECT 'Assets', COUNT(*) FROM assets
UNION ALL
SELECT 'Market Data', COUNT(*) FROM market_data;

-- View sample data
SELECT * FROM portfolios;
SELECT * FROM assets LIMIT 5;
SELECT * FROM market_data WHERE ticker = 'GOOG' LIMIT 5;
```

---

## 5. Load CSV Data (Optional)

If you have the `stock_dataset.csv` file, you can load it using:

### Method 1: MySQL LOAD DATA

```sql
LOAD DATA LOCAL INFILE '/path/to/stock_dataset.csv'
INTO TABLE market_data
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(@date, @open, @high, @low, @close, @volume, ticker)
SET
  date = STR_TO_DATE(@date, '%d-%m-%Y'),
  open_price = @open,
  high_price = @high,
  low_price = @low,
  close_price = @close,
  volume = @volume;
```

### Method 2: Using MySQL Workbench

1. Right-click on `market_data` table
2. Select "Table Data Import Wizard"
3. Browse to `stock_dataset.csv`
4. Map columns:
   - Date → date
   - Open → open_price
   - High → high_price
   - Low → low_price
   - Close → close_price
   - Volume → volume
   - Ticker → ticker
5. Click "Next" and "Finish"

---

## 6. Useful Queries

### Get Portfolio with Total Value

```sql
SELECT 
    p.portfolio_id,
    p.portfolio_name,
    p.cash_balance,
    COALESCE(SUM(a.quantity * a.current_price), 0) as assets_value,
    p.cash_balance + COALESCE(SUM(a.quantity * a.current_price), 0) as total_value
FROM portfolios p
LEFT JOIN assets a ON p.id = a.portfolio_id
GROUP BY p.id, p.portfolio_id, p.portfolio_name, p.cash_balance;
```

### Get Asset Performance

```sql
SELECT 
    a.asset_id,
    a.ticker,
    a.asset_name,
    a.quantity,
    a.purchase_price,
    a.current_price,
    (a.current_price - a.purchase_price) as price_change,
    ((a.current_price - a.purchase_price) / a.purchase_price * 100) as gain_loss_percentage,
    (a.quantity * a.current_price) as total_value,
    (a.quantity * (a.current_price - a.purchase_price)) as total_gain_loss
FROM assets a
WHERE a.purchase_price IS NOT NULL
ORDER BY gain_loss_percentage DESC;
```

### Get Latest Market Prices

```sql
SELECT 
    ticker,
    date,
    close_price,
    volume
FROM market_data md1
WHERE date = (
    SELECT MAX(date) 
    FROM market_data md2 
    WHERE md2.ticker = md1.ticker
)
ORDER BY ticker;
```

### Get Price History for a Ticker

```sql
SELECT 
    date,
    open_price,
    high_price,
    low_price,
    close_price,
    volume
FROM market_data
WHERE ticker = 'GOOG'
  AND date BETWEEN '2026-01-01' AND '2026-01-31'
ORDER BY date ASC;
```

---

## 7. Backup and Restore

### Backup Database

```bash
mysqldump -u portfolio_user -p portfolio_manager > portfolio_manager_backup.sql
```

### Restore Database

```bash
mysql -u portfolio_user -p portfolio_manager < portfolio_manager_backup.sql
```

---

## 8. Drop Database (Caution!)

```sql
-- WARNING: This will delete all data!
DROP DATABASE IF EXISTS portfolio_manager;
DROP USER IF EXISTS 'portfolio_user'@'localhost';
```

---

## 9. Performance Optimization

### Add Additional Indexes

```sql
-- Index for faster portfolio lookups
CREATE INDEX idx_portfolio_name ON portfolios(portfolio_name);

-- Index for asset searches
CREATE INDEX idx_asset_name ON assets(asset_name);

-- Composite index for market data queries
CREATE INDEX idx_ticker_date_close ON market_data(ticker, date, close_price);
```

### Analyze Tables

```sql
ANALYZE TABLE portfolios;
ANALYZE TABLE assets;
ANALYZE TABLE market_data;
```

---

## 10. Database Statistics

```sql
-- Table sizes
SELECT 
    table_name AS 'Table',
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)'
FROM information_schema.TABLES
WHERE table_schema = 'portfolio_manager'
ORDER BY (data_length + index_length) DESC;

-- Row counts
SELECT 
    table_name AS 'Table',
    table_rows AS 'Rows'
FROM information_schema.TABLES
WHERE table_schema = 'portfolio_manager';
```

---

## Connection String for Application

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/portfolio_manager?useSSL=false&serverTimezone=UTC
spring.datasource.username=portfolio_user
spring.datasource.password=Portfolio@123
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

---

## Troubleshooting

### Issue: Cannot connect to database
**Solution**: Check if MySQL service is running
```bash
# Windows
net start MySQL80

# Linux/Mac
sudo systemctl start mysql
```

### Issue: Access denied for user
**Solution**: Verify user credentials and permissions
```sql
SHOW GRANTS FOR 'portfolio_user'@'localhost';
```

### Issue: Table doesn't exist
**Solution**: Run the CREATE TABLE statements again

### Issue: CSV import fails
**Solution**: Check file path and enable local infile
```sql
SET GLOBAL local_infile = 1;
```

---

**Last Updated**: February 2, 2026  
**Database Version**: MySQL 8.0
