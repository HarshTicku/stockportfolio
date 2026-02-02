# Portfolio Manager - API Quick Reference

## Base URL
```
http://localhost:8080/api/v1
```

## Quick Start Examples

### 1. Create a Portfolio
```bash
curl -X POST http://localhost:8080/api/v1/portfolios \
  -H "Content-Type: application/json" \
  -d '{
    "portfolioName": "My Portfolio",
    "baseCurrency": "USD",
    "cashBalance": 50000.00
  }'
```

### 2. Add an Asset
```bash
curl -X POST http://localhost:8080/api/v1/assets \
  -H "Content-Type: application/json" \
  -d '{
    "portfolioId": "PORT-001",
    "ticker": "AAPL",
    "assetName": "Apple Inc.",
    "assetType": "STOCK",
    "quantity": 100,
    "purchasePrice": 150.00
  }'
```

### 3. Get Latest Stock Price
```bash
curl -X GET http://localhost:8080/api/v1/market-data/AAPL/latest
```

### 4. View Portfolio Summary
```bash
curl -X GET http://localhost:8080/api/v1/portfolios/PORT-001/summary
```

---

## All Endpoints

### Portfolios
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/portfolios` | Create portfolio |
| GET | `/portfolios` | List all portfolios |
| GET | `/portfolios/{id}` | Get portfolio details |
| PUT | `/portfolios/{id}` | Update portfolio |
| DELETE | `/portfolios/{id}` | Delete portfolio |
| GET | `/portfolios/{id}/summary` | Portfolio summary |
| GET | `/portfolios/{id}/assets` | Portfolio assets |

### Assets
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/assets` | Create asset |
| GET | `/assets` | List all assets |
| GET | `/assets/{id}` | Get asset details |
| PUT | `/assets/{id}` | Update asset |
| DELETE | `/assets/{id}` | Delete asset |

### Market Data
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/market-data/{ticker}` | Historical data |
| GET | `/market-data/{ticker}/latest` | Latest price |
| GET | `/market-data/latest?tickers=X,Y` | Multiple prices |
| GET | `/market-data/{ticker}/history` | Price history |
| GET | `/market-data/tickers` | Supported tickers |

---

## Supported Tickers
- **GOOG** - Alphabet Inc.
- **AAPL** - Apple Inc.
- **MSFT** - Microsoft Corporation
- **AMZN** - Amazon.com Inc.
- **META** - Meta Platforms Inc.
- **NFLX** - Netflix Inc.

---

## Response Codes
- `200` - Success
- `201` - Created
- `204` - Deleted
- `400` - Bad Request
- `404` - Not Found
- `409` - Conflict
- `500` - Server Error

---

## Sample Request Bodies

### Create Portfolio
```json
{
  "portfolioName": "Growth Portfolio",
  "description": "Tech stocks",
  "baseCurrency": "USD",
  "cashBalance": 100000.00
}
```

### Create Asset
```json
{
  "portfolioId": "PORT-001",
  "ticker": "GOOG",
  "assetName": "Alphabet Inc.",
  "assetType": "STOCK",
  "quantity": 50,
  "purchasePrice": 250.00,
  "purchaseDate": "2025-06-15",
  "notes": "Long-term investment"
}
```

### Update Asset
```json
{
  "quantity": 75,
  "purchasePrice": 260.00,
  "notes": "Added more shares"
}
```

---

## Asset Types
- `STOCK` - Stocks/Equities
- `BOND` - Bonds
- `CASH` - Cash holdings
- `ETF` - Exchange-Traded Funds
- `CRYPTO` - Cryptocurrencies
- `COMMODITY` - Commodities

---

## Common Queries

### Filter assets by portfolio
```bash
curl -X GET "http://localhost:8080/api/v1/assets?portfolioId=PORT-001"
```

### Filter assets by type
```bash
curl -X GET "http://localhost:8080/api/v1/assets?assetType=STOCK"
```

### Get market data for date range
```bash
curl -X GET "http://localhost:8080/api/v1/market-data/GOOG/history?startDate=2026-01-01&endDate=2026-01-31"
```

### Pagination
```bash
curl -X GET "http://localhost:8080/api/v1/portfolios?page=0&size=20"
```

---

## Database Tables

### portfolios
- `portfolio_id` - Unique ID
- `portfolio_name` - Name
- `base_currency` - Currency (USD, EUR, etc.)
- `total_value` - Total portfolio value
- `cash_balance` - Available cash

### assets
- `asset_id` - Unique ID
- `portfolio_id` - Foreign key
- `ticker` - Stock symbol
- `asset_type` - Type (STOCK, BOND, etc.)
- `quantity` - Number of units
- `purchase_price` - Buy price
- `current_price` - Current market price

### market_data
- `ticker` - Stock symbol
- `date` - Trading date
- `open_price` - Opening price
- `high_price` - Day high
- `low_price` - Day low
- `close_price` - Closing price
- `volume` - Trading volume

---

## Testing with Postman

1. Import OpenAPI spec from: `http://localhost:8080/v3/api-docs`
2. Set base URL: `http://localhost:8080/api/v1`
3. Create requests for each endpoint
4. Test CRUD operations

---

## Swagger UI

Access interactive API documentation:
```
http://localhost:8080/swagger-ui.html
```

---

**For detailed documentation, see**: `backend_design_rishit.md`
