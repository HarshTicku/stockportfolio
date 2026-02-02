# Portfolio Manager - Backend Design Document

## Table of Contents
1. [Overview](#overview)
2. [Technology Stack](#technology-stack)
3. [System Architecture](#system-architecture)
4. [Database Design](#database-design)
5. [API Endpoints](#api-endpoints)
6. [Implementation Guide](#implementation-guide)

---

## Overview

Portfolio Manager is a financial application that combines:
- **Transaction Monitoring** - Track and monitor financial transactions
- **Alert System** - Rule-based monitoring with configurable alerts
- **Payment Processing** - Manage payment lifecycle
- **Portfolio Management** - Track holdings, AI predictions, and optimization

### Architecture Approach
- **Monolithic Spring Boot Application** - Simple, easy to deploy and maintain
- **MySQL Database** - Single relational database for all data
- **RESTful APIs** - Standard REST endpoints with JSON
- **Layered Architecture** - Controller → Service → Repository pattern

---

## Technology Stack

### Backend
- **Framework**: Spring Boot 3.2
- **Language**: Java 17
- **Build Tool**: Maven
- **ORM**: Spring Data JPA (Hibernate)

### Database
- **Database**: MySQL 8.0
- **Migration**: Flyway or Liquibase
- **Connection Pool**: HikariCP (default with Spring Boot)

### Additional Libraries
- **Validation**: Hibernate Validator
- **API Docs**: Springdoc OpenAPI (Swagger)
- **Logging**: SLF4J with Logback
- **Testing**: JUnit 5, Mockito
- **Utilities**: Lombok, MapStruct

### Optional Enhancements (If Time Permits)
- **Caching**: Spring Cache with Caffeine
- **Async Processing**: Spring @Async
- **Scheduler**: Spring @Scheduled for periodic tasks
- **WebSocket**: Spring WebSocket for real-time updates

---

## System Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────┐
│                 Frontend (React/Vue)                 │
└──────────────────────┬──────────────────────────────┘
                       │ HTTP/REST (JSON)
┌──────────────────────▼──────────────────────────────┐
│              Spring Boot Application                 │
│                                                       │
│  ┌────────────────────────────────────────────────┐ │
│  │         REST Controllers                        │ │
│  │  - TransactionController                        │ │
│  │  - PaymentController                            │ │
│  │  - AlertController                              │ │
│  │  - RuleController                               │ │
│  │  - PortfolioController                          │ │
│  └──────────────────┬─────────────────────────────┘ │
│                     │                                 │
│  ┌──────────────────▼─────────────────────────────┐ │
│  │         Service Layer                           │ │
│  │  - Business Logic                               │ │
│  │  - Validation                                   │ │
│  │  - Rule Evaluation                              │ │
│  │  - Alert Generation                             │ │
│  └──────────────────┬─────────────────────────────┘ │
│                     │                                 │
│  ┌──────────────────▼─────────────────────────────┐ │
│  │         Repository Layer (JPA)                  │ │
│  │  - Data Access                                  │ │
│  │  - Custom Queries                               │ │
│  └──────────────────┬─────────────────────────────┘ │
└────────────────────┬┬────────────────────────────────┘
                     ││
┌────────────────────▼▼────────────────────────────────┐
│                MySQL Database                         │
│  - transactions                                       │
│  - payments                                           │
│  - payment_history                                    │
│  - alerts                                             │
│  - alert_transactions                                 │
│  - rules                                              │
│  - portfolios                                         │
│  - holdings                                           │
└───────────────────────────────────────────────────────┘
```

### Package Structure

```
com.portfoliomanager
├── controller          (REST endpoints)
├── service            (Business logic)
├── repository         (Data access)
├── entity             (JPA entities)
├── dto                (Data transfer objects)
├── mapper             (Entity ↔ DTO converters)
├── config             (Spring configuration)
├── exception          (Custom exceptions & handlers)
└── util               (Helper utilities)
```

---

## Database Design

### ERD Overview

```
┌─────────────────┐
│  transactions   │
└────────┬────────┘
         │
         │ (referenced by)
         │
┌────────▼─────────────┐      ┌──────────────┐
│ alert_transactions   │──────│    alerts    │
└──────────────────────┘      └──────┬───────┘
                                     │
                              ┌──────▼──────┐
                              │    rules    │
                              └─────────────┘

┌─────────────────┐      ┌─────────────────────┐
│    payments     │──────│  payment_history    │
└─────────────────┘      └─────────────────────┘

┌─────────────────┐      ┌─────────────────┐
│   portfolios    │──────│    holdings     │
└─────────────────┘      └─────────────────┘
```

### Database Schema

#### 1. Transactions Table

```sql
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(50) UNIQUE NOT NULL,
    account_id VARCHAR(50) NOT NULL,
    counterparty_id VARCHAR(50),
    counterparty_name VARCHAR(255),
    type ENUM('DEBIT', 'CREDIT') NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_account_timestamp (account_id, timestamp),
    INDEX idx_timestamp (timestamp),
    INDEX idx_counterparty (counterparty_id),
    INDEX idx_transaction_id (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 2. Payments Table

```sql
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id VARCHAR(50) UNIQUE NOT NULL,
    idempotency_key VARCHAR(255) UNIQUE,
    source_account VARCHAR(50) NOT NULL,
    destination_account VARCHAR(50) NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status ENUM('CREATED', 'VALIDATED', 'SENT', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'CREATED',
    reference VARCHAR(255),
    description TEXT,
    error_code VARCHAR(50),
    error_message TEXT,
    retry_count INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    validated_at DATETIME NULL,
    sent_at DATETIME NULL,
    completed_at DATETIME NULL,
    failed_at DATETIME NULL,
    
    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_accounts_different CHECK (source_account != destination_account),
    
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_payment_id (payment_id),
    INDEX idx_idempotency_key (idempotency_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 3. Payment Status History Table

```sql
CREATE TABLE payment_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    from_status VARCHAR(20),
    to_status VARCHAR(20) NOT NULL,
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    triggered_by VARCHAR(100),
    
    FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE,
    INDEX idx_payment_timestamp (payment_id, timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 4. Rules Table

```sql
CREATE TABLE rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_id VARCHAR(50) UNIQUE NOT NULL,
    rule_name VARCHAR(255) NOT NULL,
    rule_type ENUM('AMOUNT_THRESHOLD', 'VELOCITY', 'NEW_PAYEE', 'DAILY_LIMIT') NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    severity ENUM('HIGH', 'MEDIUM', 'LOW') NOT NULL DEFAULT 'MEDIUM',
    parameters JSON NOT NULL,
    description TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_active (active),
    INDEX idx_rule_type (rule_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**Example Rule Parameters (JSON)**:

```json
-- Amount Threshold Rule
{
  "threshold": 10000.00,
  "currency": "USD",
  "transactionType": "DEBIT"
}

-- Velocity Rule
{
  "maxTransactions": 5,
  "timeWindowMinutes": 10,
  "scope": "PER_ACCOUNT"
}

-- New Payee Rule
{
  "scope": "PER_ACCOUNT",
  "lookbackDays": 90
}

-- Daily Limit Rule
{
  "dailyLimit": 50000.00,
  "currency": "USD",
  "scope": "PER_ACCOUNT"
}
```

#### 5. Alerts Table

```sql
CREATE TABLE alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alert_id VARCHAR(50) UNIQUE NOT NULL,
    rule_id BIGINT NOT NULL,
    severity ENUM('HIGH', 'MEDIUM', 'LOW') NOT NULL,
    status ENUM('OPEN', 'ACKNOWLEDGED', 'INVESTIGATING', 'CLOSED', 'DISMISSED') NOT NULL DEFAULT 'OPEN',
    message TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    acknowledged_at DATETIME NULL,
    acknowledged_by VARCHAR(100),
    closed_at DATETIME NULL,
    closed_by VARCHAR(100),
    resolution_notes TEXT,
    
    FOREIGN KEY (rule_id) REFERENCES rules(id),
    INDEX idx_status (status),
    INDEX idx_severity (severity),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 6. Alert-Transaction Link Table

```sql
CREATE TABLE alert_transactions (
    alert_id BIGINT NOT NULL,
    transaction_id BIGINT NOT NULL,
    
    PRIMARY KEY (alert_id, transaction_id),
    FOREIGN KEY (alert_id) REFERENCES alerts(id) ON DELETE CASCADE,
    FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE,
    INDEX idx_transaction (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 7. Portfolios Table

```sql
CREATE TABLE portfolios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id VARCHAR(50) UNIQUE NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    portfolio_name VARCHAR(255) NOT NULL,
    base_currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    initial_value DECIMAL(18, 2),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 8. Holdings Table

```sql
CREATE TABLE holdings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    asset_symbol VARCHAR(20) NOT NULL,
    asset_name VARCHAR(255),
    asset_type ENUM('STOCK', 'BOND', 'CRYPTO', 'ETF', 'COMMODITY') NOT NULL,
    quantity DECIMAL(18, 8) NOT NULL,
    average_cost DECIMAL(18, 2),
    current_price DECIMAL(18, 2),
    last_price_update DATETIME,
    target_allocation DECIMAL(5, 2),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE,
    UNIQUE KEY unique_portfolio_asset (portfolio_id, asset_symbol),
    INDEX idx_portfolio (portfolio_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## API Endpoints

### Base URL
```
http://localhost:8080/api/v1
```

### 1. Transaction API

#### Create Transaction
```http
POST /api/v1/transactions
Content-Type: application/json

{
  "accountId": "ACC-123456",
  "counterpartyId": "PAYEE-789012",
  "counterpartyName": "ACME Corp",
  "type": "DEBIT",
  "amount": 15000.00,
  "currency": "USD",
  "description": "Payment for services"
}

Response: 201 Created
{
  "transactionId": "TXN-20260201-001234",
  "accountId": "ACC-123456",
  "amount": 15000.00,
  "currency": "USD",
  "status": "COMPLETED",
  "timestamp": "2026-02-01T23:30:00Z"
}
```

#### List Transactions
```http
GET /api/v1/transactions?page=0&size=20&accountId=ACC-123456&startDate=2026-01-01&endDate=2026-02-01

Response: 200 OK
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8
}
```

#### Get Transaction Details
```http
GET /api/v1/transactions/{transactionId}

Response: 200 OK
{
  "transactionId": "TXN-20260201-001234",
  "accountId": "ACC-123456",
  "counterpartyId": "PAYEE-789012",
  "counterpartyName": "ACME Corp",
  "type": "DEBIT",
  "amount": 15000.00,
  "currency": "USD",
  "timestamp": "2026-02-01T23:30:00Z",
  "description": "Payment for services",
  "status": "COMPLETED"
}
```

#### Search Transactions
```http
GET /api/v1/transactions/search?query=ACME&minAmount=1000&maxAmount=20000

Response: 200 OK
{
  "results": [...]
}
```

---

### 2. Payment API

#### Create Payment
```http
POST /api/v1/payments
Content-Type: application/json
Idempotency-Key: unique-key-123

{
  "sourceAccount": "ACC-001-123456",
  "destinationAccount": "ACC-002-789012",
  "amount": 1500.00,
  "currency": "USD",
  "reference": "Invoice-2026-001",
  "description": "Payment for consulting services"
}

Response: 201 Created
{
  "paymentId": "PAY-12345678",
  "status": "CREATED",
  "sourceAccount": "ACC-001-123456",
  "destinationAccount": "ACC-002-789012",
  "amount": 1500.00,
  "currency": "USD",
  "createdAt": "2026-02-01T23:30:00Z"
}
```

#### List Payments
```http
GET /api/v1/payments?status=COMPLETED&page=0&size=20

Response: 200 OK
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 45,
  "totalPages": 3
}
```

#### Get Payment Details
```http
GET /api/v1/payments/{paymentId}

Response: 200 OK
{
  "paymentId": "PAY-12345678",
  "sourceAccount": "ACC-001-123456",
  "destinationAccount": "ACC-002-789012",
  "amount": 1500.00,
  "currency": "USD",
  "status": "COMPLETED",
  "createdAt": "2026-02-01T23:30:00Z",
  "completedAt": "2026-02-01T23:30:45Z"
}
```

#### Get Payment History
```http
GET /api/v1/payments/{paymentId}/history

Response: 200 OK
[
  {
    "fromStatus": null,
    "toStatus": "CREATED",
    "timestamp": "2026-02-01T23:30:00Z",
    "notes": "Payment initiated"
  },
  {
    "fromStatus": "CREATED",
    "toStatus": "VALIDATED",
    "timestamp": "2026-02-01T23:30:15Z",
    "notes": "All validation checks passed"
  },
  {
    "fromStatus": "VALIDATED",
    "toStatus": "SENT",
    "timestamp": "2026-02-01T23:30:30Z"
  },
  {
    "fromStatus": "SENT",
    "toStatus": "COMPLETED",
    "timestamp": "2026-02-01T23:30:45Z",
    "notes": "Payment successfully processed"
  }
]
```

#### Update Payment Status (Internal)
```http
PUT /api/v1/payments/{paymentId}/status
Content-Type: application/json

{
  "status": "VALIDATED",
  "notes": "Validation successful"
}

Response: 200 OK
```

#### Retry Failed Payment
```http
POST /api/v1/payments/{paymentId}/retry

Response: 200 OK
{
  "paymentId": "PAY-12345678",
  "status": "CREATED",
  "retryCount": 1
}
```

---

### 3. Alert API

#### List Alerts
```http
GET /api/v1/alerts?status=OPEN&severity=HIGH&page=0&size=20

Response: 200 OK
{
  "content": [
    {
      "alertId": "ALERT-20260201-5678",
      "severity": "HIGH",
      "status": "OPEN",
      "message": "Transaction amount exceeds threshold of $10,000",
      "createdAt": "2026-02-01T23:30:46Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 12
}
```

#### Get Alert Details
```http
GET /api/v1/alerts/{alertId}

Response: 200 OK
{
  "alertId": "ALERT-20260201-5678",
  "ruleId": "RULE-001",
  "ruleName": "High Value Transaction",
  "severity": "HIGH",
  "status": "OPEN",
  "message": "Transaction amount exceeds threshold of $10,000",
  "createdAt": "2026-02-01T23:30:46Z",
  "transactions": [
    {
      "transactionId": "TXN-20260201-001234",
      "amount": 15000.00,
      "timestamp": "2026-02-01T23:30:00Z"
    }
  ]
}
```

#### Acknowledge Alert
```http
PUT /api/v1/alerts/{alertId}/acknowledge
Content-Type: application/json

{
  "acknowledgedBy": "operator-1"
}

Response: 200 OK
{
  "alertId": "ALERT-20260201-5678",
  "status": "ACKNOWLEDGED",
  "acknowledgedAt": "2026-02-01T23:35:00Z",
  "acknowledgedBy": "operator-1"
}
```

#### Update Alert Status
```http
PUT /api/v1/alerts/{alertId}/status
Content-Type: application/json

{
  "status": "INVESTIGATING",
  "userId": "operator-1"
}

Response: 200 OK
```

#### Close Alert
```http
PUT /api/v1/alerts/{alertId}/close
Content-Type: application/json

{
  "resolutionNotes": "Verified transaction is legitimate",
  "closedBy": "operator-1"
}

Response: 200 OK
```

#### Add Alert Notes
```http
POST /api/v1/alerts/{alertId}/notes
Content-Type: application/json

{
  "notes": "Investigation findings: Customer verified purchase"
}

Response: 200 OK
```

#### Get Alert Statistics
```http
GET /api/v1/alerts/stats

Response: 200 OK
{
  "totalAlerts": 150,
  "openAlerts": 12,
  "acknowledgedAlerts": 8,
  "closedAlerts": 130,
  "highSeverity": 15,
  "mediumSeverity": 45,
  "lowSeverity": 90,
  "avgResolutionTimeMinutes": 45.5
}
```

---

### 4. Rules API

#### List All Rules
```http
GET /api/v1/rules?active=true

Response: 200 OK
[
  {
    "ruleId": "RULE-001",
    "ruleName": "High Value Transaction",
    "ruleType": "AMOUNT_THRESHOLD",
    "active": true,
    "severity": "HIGH",
    "parameters": {
      "threshold": 10000.00,
      "currency": "USD",
      "transactionType": "DEBIT"
    }
  }
]
```

#### Get Rule Details
```http
GET /api/v1/rules/{ruleId}

Response: 200 OK
{
  "ruleId": "RULE-001",
  "ruleName": "High Value Transaction",
  "ruleType": "AMOUNT_THRESHOLD",
  "active": true,
  "severity": "HIGH",
  "description": "Alert on transactions exceeding $10,000",
  "parameters": {
    "threshold": 10000.00,
    "currency": "USD",
    "transactionType": "DEBIT"
  }
}
```

#### Create Rule
```http
POST /api/v1/rules
Content-Type: application/json

{
  "ruleName": "Rapid Transactions",
  "ruleType": "VELOCITY",
  "severity": "MEDIUM",
  "description": "Alert on rapid transaction velocity",
  "parameters": {
    "maxTransactions": 5,
    "timeWindowMinutes": 10,
    "scope": "PER_ACCOUNT"
  }
}

Response: 201 Created
{
  "ruleId": "RULE-002",
  "ruleName": "Rapid Transactions",
  "ruleType": "VELOCITY",
  "active": true,
  "severity": "MEDIUM"
}
```

#### Update Rule
```http
PUT /api/v1/rules/{ruleId}
Content-Type: application/json

{
  "ruleName": "High Value Transaction (Updated)",
  "severity": "MEDIUM",
  "parameters": {
    "threshold": 15000.00,
    "currency": "USD",
    "transactionType": "DEBIT"
  }
}

Response: 200 OK
```

#### Activate/Deactivate Rule
```http
PUT /api/v1/rules/{ruleId}/activate

Response: 200 OK

PUT /api/v1/rules/{ruleId}/deactivate

Response: 200 OK
```

#### Delete Rule
```http
DELETE /api/v1/rules/{ruleId}

Response: 204 No Content
```

---

### 5. Portfolio API

#### Get Portfolio Overview
```http
GET /api/v1/portfolio

Response: 200 OK
{
  "portfolioId": "PORT-001",
  "portfolioName": "My Investment Portfolio",
  "baseCurrency": "USD",
  "totalValue": 125000.00,
  "cashValue": 15000.00,
  "holdingsValue": 110000.00,
  "dailyReturn": 1.25,
  "totalReturn": 15.5,
  "lastUpdated": "2026-02-01T23:30:00Z"
}
```

#### Get Holdings
```http
GET /api/v1/portfolio/holdings

Response: 200 OK
[
  {
    "assetSymbol": "AAPL",
    "assetName": "Apple Inc.",
    "assetType": "STOCK",
    "quantity": 100,
    "averageCost": 150.00,
    "currentPrice": 175.50,
    "totalValue": 17550.00,
    "gain": 2550.00,
    "gainPercentage": 17.00,
    "allocation": 14.04
  },
  {
    "assetSymbol": "BTC",
    "assetName": "Bitcoin",
    "assetType": "CRYPTO",
    "quantity": 0.5,
    "averageCost": 40000.00,
    "currentPrice": 45000.00,
    "totalValue": 22500.00,
    "gain": 2500.00,
    "gainPercentage": 12.50,
    "allocation": 18.00
  }
]
```

#### Add/Update Holding
```http
POST /api/v1/portfolio/holdings
Content-Type: application/json

{
  "assetSymbol": "GOOGL",
  "assetName": "Alphabet Inc.",
  "assetType": "STOCK",
  "quantity": 50,
  "averageCost": 140.00
}

Response: 201 Created
```

#### Get Portfolio Performance
```http
GET /api/v1/portfolio/performance?period=1M

Response: 200 OK
{
  "period": "1M",
  "totalReturn": 3.5,
  "benchmarkReturn": 2.8,
  "volatility": 12.5,
  "sharpeRatio": 1.45,
  "maxDrawdown": -5.2,
  "performanceData": [
    {
      "date": "2026-01-01",
      "value": 120000.00,
      "return": 0.0
    },
    {
      "date": "2026-01-15",
      "value": 122500.00,
      "return": 2.08
    },
    {
      "date": "2026-02-01",
      "value": 125000.00,
      "return": 4.17
    }
  ]
}
```

#### AI Price Predictions
```http
GET /api/v1/portfolio/predictions?symbols=AAPL,GOOGL,BTC

Response: 200 OK
{
  "predictions": [
    {
      "symbol": "AAPL",
      "currentPrice": 175.50,
      "predictions": {
        "1day": 176.25,
        "7day": 178.50,
        "30day": 182.00
      },
      "confidence": 0.75
    },
    {
      "symbol": "BTC",
      "currentPrice": 45000.00,
      "predictions": {
        "1day": 45500.00,
        "7day": 47000.00,
        "30day": 50000.00
      },
      "confidence": 0.68
    }
  ],
  "generatedAt": "2026-02-01T23:30:00Z"
}
```

#### Portfolio Optimization
```http
GET /api/v1/portfolio/optimize?targetReturn=12&maxRisk=15

Response: 200 OK
{
  "currentAllocation": {
    "AAPL": 14.04,
    "GOOGL": 12.00,
    "BTC": 18.00,
    "ETH": 10.00,
    "BONDS": 45.96
  },
  "optimizedAllocation": {
    "AAPL": 20.00,
    "GOOGL": 15.00,
    "BTC": 25.00,
    "ETH": 15.00,
    "BONDS": 25.00
  },
  "expectedReturn": 12.5,
  "expectedRisk": 14.2,
  "sharpeRatio": 0.88,
  "recommendations": [
    {
      "action": "BUY",
      "symbol": "AAPL",
      "currentAllocation": 14.04,
      "targetAllocation": 20.00,
      "amountToInvest": 7450.00
    },
    {
      "action": "SELL",
      "symbol": "BONDS",
      "currentAllocation": 45.96,
      "targetAllocation": 25.00,
      "amountToSell": 26200.00
    }
  ]
}
```

#### Natural Language Query
```http
POST /api/v1/portfolio/query
Content-Type: application/json

{
  "query": "What was my best performing asset last month?"
}

Response: 200 OK
{
  "query": "What was my best performing asset last month?",
  "answer": "Your best performing asset last month was Bitcoin (BTC) with a return of 15.5%",
  "data": {
    "symbol": "BTC",
    "name": "Bitcoin",
    "return": 15.5,
    "period": "1M"
  }
}
```

#### Quantum Optimization
```http
GET /api/v1/portfolio/quantum-optimize?algorithm=QAOA

Response: 200 OK
{
  "jobId": "QOJ-123456",
  "status": "COMPLETED",
  "algorithm": "QAOA",
  "allocation": {
    "AAPL": 18.5,
    "GOOGL": 16.2,
    "TSLA": 12.3,
    "BTC": 22.0,
    "ETH": 14.0,
    "BONDS": 17.0
  },
  "expectedReturn": 13.2,
  "expectedRisk": 14.8,
  "sharpeRatio": 0.89,
  "iterations": 1000,
  "convergence": 0.0001,
  "executionTimeMs": 2500
}
```

---

## Implementation Guide

### Step 1: Project Setup

#### 1.1 Create Spring Boot Project

Using Spring Initializr (https://start.spring.io/):
- **Project**: Maven
- **Language**: Java
- **Spring Boot**: 3.2.x
- **Java**: 17
- **Dependencies**:
  - Spring Web
  - Spring Data JPA
  - MySQL Driver
  - Validation
  - Lombok
  - Springdoc OpenAPI

#### 1.2 Project Structure

```
portfolio-manager/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/portfoliomanager/
│   │   │       ├── PortfolioManagerApplication.java
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       ├── entity/
│   │   │       ├── dto/
│   │   │       ├── mapper/
│   │   │       ├── config/
│   │   │       ├── exception/
│   │   │       └── util/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/
│   └── test/
├── pom.xml
└── README.md
```

### Step 2: Configuration

#### application.yml

```yaml
spring:
  application:
    name: portfolio-manager

  datasource:
    url: jdbc:mysql://localhost:3306/portfolio_manager?useSSL=false&serverTimezone=UTC
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
    
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
    open-in-view: false

  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration

server:
  port: 8080
  servlet:
    context-path: /api/v1

logging:
  level:
    root: INFO
    com.portfoliomanager: DEBUG
    org.hibernate.SQL: DEBUG

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
```

### Step 3: Entity Examples

#### Transaction Entity

```java
package com.portfoliomanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_account_timestamp", columnList = "account_id, timestamp"),
    @Index(name = "idx_timestamp", columnList = "timestamp"),
    @Index(name = "idx_transaction_id", columnList = "transaction_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "transaction_id", unique = true, nullable = false, length = 50)
    private String transactionId;
    
    @Column(name = "account_id", nullable = false, length = 50)
    private String accountId;
    
    @Column(name = "counterparty_id", length = 50)
    private String counterpartyId;
    
    @Column(name = "counterparty_name")
    private String counterpartyName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;
    
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false, length = 3)
    private String currency = "USD";
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, length = 20)
    private String status = "COMPLETED";
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

#### Payment Entity

```java
package com.portfoliomanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_payment_id", columnList = "payment_id"),
    @Index(name = "idx_idempotency_key", columnList = "idempotency_key")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "payment_id", unique = true, nullable = false, length = 50)
    private String paymentId;
    
    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;
    
    @Column(name = "source_account", nullable = false, length = 50)
    private String sourceAccount;
    
    @Column(name = "destination_account", nullable = false, length = 50)
    private String destinationAccount;
    
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false, length = 3)
    private String currency = "USD";
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.CREATED;
    
    @Column(length = 255)
    private String reference;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "error_code", length = 50)
    private String errorCode;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "retry_count")
    private Integer retryCount = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "validated_at")
    private LocalDateTime validatedAt;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "failed_at")
    private LocalDateTime failedAt;
    
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PaymentHistory> history = new ArrayList<>();
    
    public void addHistory(PaymentHistory historyEntry) {
        history.add(historyEntry);
        historyEntry.setPayment(this);
    }
}
```

### Step 4: Repository Examples

```java
package com.portfoliomanager.repository;

import com.portfoliomanager.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    Optional<Transaction> findByTransactionId(String transactionId);
    
    Page<Transaction> findByAccountId(String accountId, Pageable pageable);
    
    Page<Transaction> findByAccountIdAndTimestampBetween(
        String accountId, 
        LocalDateTime start, 
        LocalDateTime end, 
        Pageable pageable
    );
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.accountId = :accountId " +
           "AND t.timestamp > :since")
    long countRecentTransactions(String accountId, LocalDateTime since);
    
    @Query("SELECT t FROM Transaction t WHERE t.counterpartyName LIKE %:query% " +
           "OR t.description LIKE %:query%")
    Page<Transaction> searchTransactions(String query, Pageable pageable);
}
```

```java
package com.portfoliomanager.repository;

import com.portfoliomanager.entity.Payment;
import com.portfoliomanager.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByPaymentId(String paymentId);
    
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
    
    Page<Payment> findBySourceAccount(String sourceAccount, Pageable pageable);
}
```

### Step 5: Service Layer Example

```java
package com.portfoliomanager.service;

import com.portfoliomanager.dto.PaymentRequest;
import com.portfoliomanager.dto.PaymentResponse;
import com.portfoliomanager.entity.Payment;
import com.portfoliomanager.entity.PaymentHistory;
import com.portfoliomanager.entity.PaymentStatus;
import com.portfoliomanager.exception.DuplicatePaymentException;
import com.portfoliomanager.exception.PaymentNotFoundException;
import com.portfoliomanager.mapper.PaymentMapper;
import com.portfoliomanager.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request, String idempotencyKey) {
        // Check for duplicate
        if (idempotencyKey != null) {
            return paymentRepository.findByIdempotencyKey(idempotencyKey)
                .map(paymentMapper::toResponse)
                .orElseGet(() -> createNewPayment(request, idempotencyKey));
        }
        return createNewPayment(request, null);
    }
    
    private PaymentResponse createNewPayment(PaymentRequest request, String idempotencyKey) {
        // Validate payment
        validatePayment(request);
        
        // Create payment
        Payment payment = Payment.builder()
            .paymentId(generatePaymentId())
            .idempotencyKey(idempotencyKey)
            .sourceAccount(request.getSourceAccount())
            .destinationAccount(request.getDestinationAccount())
            .amount(request.getAmount())
            .currency(request.getCurrency())
            .reference(request.getReference())
            .description(request.getDescription())
            .status(PaymentStatus.CREATED)
            .build();
        
        // Add history
        addHistoryEntry(payment, null, PaymentStatus.CREATED, "Payment initiated");
        
        // Save
        payment = paymentRepository.save(payment);
        
        log.info("Payment created: {}", payment.getPaymentId());
        
        // Async: Trigger validation
        processPaymentAsync(payment.getId());
        
        return paymentMapper.toResponse(payment);
    }
    
    private void validatePayment(PaymentRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be greater than 0");
        }
        if (request.getSourceAccount().equals(request.getDestinationAccount())) {
            throw new ValidationException("Source and destination accounts must be different");
        }
        // Additional validation...
    }
    
    @Transactional
    public void updatePaymentStatus(String paymentId, PaymentStatus newStatus, String notes) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        
        PaymentStatus oldStatus = payment.getStatus();
        
        // Validate state transition
        if (!isValidTransition(oldStatus, newStatus)) {
            throw new InvalidTransitionException(oldStatus, newStatus);
        }
        
        payment.setStatus(newStatus);
        updateTimestamps(payment, newStatus);
        addHistoryEntry(payment, oldStatus, newStatus, notes);
        
        paymentRepository.save(payment);
        
        log.info("Payment {} status changed: {} -> {}", paymentId, oldStatus, newStatus);
    }
    
    private boolean isValidTransition(PaymentStatus from, PaymentStatus to) {
        return switch (from) {
            case CREATED -> to == PaymentStatus.VALIDATED || to == PaymentStatus.FAILED;
            case VALIDATED -> to == PaymentStatus.SENT || to == PaymentStatus.FAILED;
            case SENT -> to == PaymentStatus.COMPLETED || to == PaymentStatus.FAILED;
            case COMPLETED, FAILED -> false;
        };
    }
    
    private void addHistoryEntry(Payment payment, PaymentStatus from, PaymentStatus to, String notes) {
        PaymentHistory history = PaymentHistory.builder()
            .fromStatus(from)
            .toStatus(to)
            .timestamp(LocalDateTime.now())
            .notes(notes)
            .build();
        payment.addHistory(history);
    }
    
    private String generatePaymentId() {
        return "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
```

### Step 6: Controller Example

```java
package com.portfoliomanager.controller;

import com.portfoliomanager.dto.PaymentRequest;
import com.portfoliomanager.dto.PaymentResponse;
import com.portfoliomanager.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment management API")
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @PostMapping
    @Operation(summary = "Create a new payment")
    public ResponseEntity<PaymentResponse> createPayment(
        @Valid @RequestBody PaymentRequest request,
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        PaymentResponse response = paymentService.createPayment(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment details")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String paymentId) {
        PaymentResponse response = paymentService.getPayment(paymentId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "List payments")
    public ResponseEntity<Page<PaymentResponse>> listPayments(
        @RequestParam(required = false) String status,
        Pageable pageable
    ) {
        Page<PaymentResponse> payments = paymentService.listPayments(status, pageable);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/{paymentId}/history")
    @Operation(summary = "Get payment history")
    public ResponseEntity<List<PaymentHistoryResponse>> getPaymentHistory(
        @PathVariable String paymentId
    ) {
        List<PaymentHistoryResponse> history = paymentService.getPaymentHistory(paymentId);
        return ResponseEntity.ok(history);
    }
}
```

### Step 7: Exception Handling

```java
package com.portfoliomanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFound(PaymentNotFoundException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("PAYMENT_NOT_FOUND")
            .message(ex.getMessage())
            .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("VALIDATION_ERROR")
            .message(ex.getMessage())
            .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
```

---

## Minimal Implementation Roadmap

### Phase 1: Core Payment System (Week 1)
1. ✅ Setup Spring Boot project
2. ✅ Configure MySQL database
3. ✅ Create Payment entity and repository
4. ✅ Implement Payment CRUD operations
5. ✅ Add payment status transitions
6. ✅ Create payment history tracking
7. ✅ Add basic validation

### Phase 2: Transaction Monitoring (Week 2)
1. ✅ Create Transaction entity
2. ✅ Implement Transaction API
3. ✅ Add Rule entity
4. ✅ Create basic rule evaluation logic
5. ✅ Implement amount threshold rule
6. ✅ Add Alert generation

### Phase 3: Alert Management (Week 3)
1. ✅ Create Alert entity
2. ✅ Implement Alert API
3. ✅ Add alert lifecycle management
4. ✅ Implement velocity rule
5. ✅ Add alert statistics

### Phase 4: Portfolio Management (Week 4)
1. ✅ Create Portfolio and Holdings entities
2. ✅ Implement Portfolio API
3. ✅ Add basic performance calculation
4. ✅ Create optimization endpoint (mock)
5. ✅ Add AI prediction endpoint (mock)

### Phase 5: Polish & Testing (Week 5)
1. ✅ Add comprehensive error handling
2. ✅ Write unit tests
3. ✅ Add integration tests
4. ✅ Complete API documentation (Swagger)
5. ✅ Add logging
6. ✅ Performance optimization

---

## Testing Strategy

### Unit Tests
```java
@SpringBootTest
class PaymentServiceTest {
    
    @MockBean
    private PaymentRepository paymentRepository;
    
    @Autowired
    private PaymentService paymentService;
    
    @Test
    void shouldCreatePayment() {
        // Arrange
        PaymentRequest request = PaymentRequest.builder()
            .sourceAccount("ACC-001")
            .destinationAccount("ACC-002")
            .amount(new BigDecimal("1500.00"))
            .currency("USD")
            .build();
        
        // Act
        PaymentResponse response = paymentService.createPayment(request, null);
        
        // Assert
        assertNotNull(response.getPaymentId());
        assertEquals(PaymentStatus.CREATED, response.getStatus());
    }
}
```

### Integration Tests
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
class PaymentControllerIT {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldCreateAndRetrievePayment() {
        // Create payment
        PaymentRequest request = new PaymentRequest(...);
        ResponseEntity<PaymentResponse> createResponse = 
            restTemplate.postForEntity("/api/v1/payments", request, PaymentResponse.class);
        
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        
        String paymentId = createResponse.getBody().getPaymentId();
        
        // Retrieve payment
        ResponseEntity<PaymentResponse> getResponse = 
            restTemplate.getForEntity("/api/v1/payments/" + paymentId, PaymentResponse.class);
        
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals(paymentId, getResponse.getBody().getPaymentId());
    }
}
```

---

## Performance Optimization Tips

### 1. Database Indexing
- Add indexes on frequently queried columns (account_id, timestamp, status)
- Use composite indexes for multi-column queries
- Monitor slow queries with MySQL slow query log

### 2. JPA Optimizations
- Use `@EntityGraph` to prevent N+1 queries
- Implement pagination for large result sets
- Use `@BatchSize` for collections
- Enable second-level cache for reference data

### 3. Connection Pooling
- Configure HikariCP appropriately
- Monitor connection pool metrics
- Set appropriate timeout values

### 4. Caching (Optional)
```java
@Service
public class RuleService {
    
    @Cacheable("active-rules")
    public List<Rule> getActiveRules() {
        return ruleRepository.findByActiveTrue();
    }
    
    @CacheEvict(value = "active-rules", allEntries = true)
    public Rule updateRule(String ruleId, RuleRequest request) {
        // Update logic
    }
}
```

---

## API Documentation (Swagger)

Access Swagger UI at: `http://localhost:8080/api/v1/swagger-ui.html`

### Example Swagger Annotations

```java
@Operation(
    summary = "Create a new payment",
    description = "Creates a new payment with the provided details. Supports idempotency via Idempotency-Key header."
)
@ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Payment created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid input"),
    @ApiResponse(responseCode = "409", description = "Duplicate payment")
})
@PostMapping
public ResponseEntity<PaymentResponse> createPayment(
    @Parameter(description = "Payment details") @Valid @RequestBody PaymentRequest request,
    @Parameter(description = "Idempotency key for duplicate prevention") 
    @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
) {
    // Implementation
}
```

---

## Summary

This simplified backend design provides:

✅ **Spring Boot monolith** - Easy to develop and deploy  
✅ **MySQL database** - Single database for all data  
✅ **RESTful APIs** - Complete API coverage for all features  
✅ **Layered architecture** - Clean separation of concerns  
✅ **Easy to implement** - Standard Spring Boot patterns  
✅ **Scalable** - Can be scaled vertically initially, horizontally later  

### Next Steps:
1. Create Spring Boot project from template
2. Set up MySQL database
3. Implement entities and repositories
4. Build service layer with business logic
5. Create REST controllers
6. Add validation and error handling
7. Write tests
8. Document APIs with Swagger
9. Deploy and test

**Good luck with your implementation!** 🚀