# Portfolio Manager - Lovable Superprompt

## Project Overview

Create a modern, professional **Financial Portfolio Manager** web application using **React + Vite + JavaScript** (NOT TypeScript). The app should connect to an existing Spring Boot REST API backend.

---

## UI Design Reference

**[ATTACH YOUR REFERENCE IMAGE HERE]**

Match the visual style, color scheme, and layout patterns from the attached reference image. The design should feel like a professional fintech/banking application.

---

## Tech Stack Requirements

```
- React 18 with Vite (JavaScript, NOT TypeScript)
- React Router v6 for navigation
- Tailwind CSS for styling
- Recharts for charts and graphs
- Axios for API calls
- React Query (TanStack Query) for data fetching
- Lucide React for icons
- date-fns for date formatting
```

---

## Backend API Configuration

**Base URL:** `http://localhost:8080/api/v1`

All API calls should use Axios with this base configuration:

```javascript
// src/services/api.js
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

export default api;
```

---

## API Endpoints & Data Structures

### 1. PORTFOLIOS API

#### GET /portfolios - List all portfolios
**Response:**
```json
{
  "content": [
    {
      "portfolioId": "PORT-123456",
      "portfolioName": "Growth Portfolio",
      "description": "Aggressive growth stocks",
      "baseCurrency": "USD",
      "totalValue": 125000.00,
      "cashBalance": 15000.00,
      "assetsValue": 110000.00,
      "assetCount": 5,
      "createdAt": "2026-02-01T10:30:00",
      "updatedAt": "2026-02-02T15:45:00"
    }
  ],
  "totalElements": 2,
  "totalPages": 1
}
```

#### GET /portfolios/{portfolioId} - Get single portfolio

#### GET /portfolios/{portfolioId}/summary - Get portfolio summary with allocations
**Response:**
```json
{
  "portfolioId": "PORT-123456",
  "portfolioName": "Growth Portfolio",
  "baseCurrency": "USD",
  "totalValue": 125000.00,
  "cashBalance": 15000.00,
  "assetsValue": 110000.00,
  "totalGain": 15500.00,
  "totalGainPercent": 14.09,
  "assetCount": 5,
  "transactionCount": 12,
  "topHoldings": [
    {
      "assetId": "ASSET-001",
      "ticker": "AAPL",
      "assetName": "Apple Inc.",
      "assetType": "STOCK",
      "quantity": 100,
      "currentPrice": 175.50,
      "totalValue": 17550.00,
      "gainLoss": 2550.00,
      "gainLossPercentage": 17.00,
      "allocation": 14.04
    }
  ],
  "allocation": [
    { "assetType": "STOCK", "value": 95000.00, "percentage": 76.00 },
    { "assetType": "ETF", "value": 15000.00, "percentage": 12.00 },
    { "assetType": "CASH", "value": 15000.00, "percentage": 12.00 }
  ]
}
```

#### POST /portfolios - Create portfolio
**Request:**
```json
{
  "portfolioName": "My Portfolio",
  "description": "Description here",
  "baseCurrency": "USD",
  "cashBalance": 10000.00
}
```

#### PUT /portfolios/{portfolioId} - Update portfolio

#### DELETE /portfolios/{portfolioId} - Delete portfolio

---

### 2. ASSETS API

#### GET /assets - List all assets
#### GET /assets/{assetId} - Get single asset
#### GET /assets/portfolio/{portfolioId} - Get assets by portfolio

**Asset Response:**
```json
{
  "assetId": "ASSET-001",
  "portfolioId": "PORT-123456",
  "ticker": "AAPL",
  "assetName": "Apple Inc.",
  "assetType": "STOCK",
  "quantity": 100,
  "purchasePrice": 150.00,
  "currentPrice": 175.50,
  "totalValue": 17550.00,
  "gainLoss": 2550.00,
  "gainLossPercentage": 17.00,
  "allocation": 14.04,
  "purchaseDate": "2025-06-15",
  "notes": "Long-term hold"
}
```

#### POST /assets - Add asset
**Request:**
```json
{
  "portfolioId": "PORT-123456",
  "ticker": "GOOG",
  "assetName": "Alphabet Inc.",
  "assetType": "STOCK",
  "quantity": 50,
  "purchasePrice": 140.00,
  "purchaseDate": "2026-01-15"
}
```

**Asset Types:** `STOCK`, `BOND`, `CASH`, `ETF`, `CRYPTO`, `COMMODITY`

#### PUT /assets/{assetId} - Update asset
#### DELETE /assets/{assetId} - Delete asset

---

### 3. MARKET DATA API

#### GET /market-data/tickers - Get available tickers
**Response:** `["AAPL", "GOOG", "MSFT", "AMZN", "META", "NFLX"]`

#### GET /market-data/{ticker}/latest - Get latest price
#### GET /market-data/{ticker}/history?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD

**Market Data Response:**
```json
{
  "ticker": "AAPL",
  "date": "2026-02-02",
  "openPrice": 174.50,
  "highPrice": 176.80,
  "lowPrice": 173.20,
  "closePrice": 175.50,
  "volume": 45000000,
  "change": 1.50,
  "changePercent": 0.86
}
```

---

### 4. STOCKS API (Finnhub Real-time)

#### GET /stocks/{symbol}/quote - Get real-time quote
**Response:**
```json
{
  "symbol": "AAPL",
  "currentPrice": 175.50,
  "change": 2.35,
  "changePercent": 1.36,
  "highPrice": 176.80,
  "lowPrice": 173.20,
  "openPrice": 174.00,
  "previousClose": 173.15,
  "timestamp": 1706889600000
}
```

#### GET /stocks/{symbol}/profile - Get company profile
**Response:**
```json
{
  "symbol": "AAPL",
  "name": "Apple Inc.",
  "country": "US",
  "currency": "USD",
  "exchange": "NASDAQ",
  "industry": "Technology",
  "logo": "https://finnhub.io/api/logo?symbol=AAPL",
  "marketCap": 2850000000000,
  "weburl": "https://www.apple.com"
}
```

#### GET /stocks/quotes?symbols=AAPL,GOOG,MSFT - Get multiple quotes
#### GET /stocks/search?query=apple - Search stocks

---

### 5. TRANSACTIONS API

#### GET /transactions/portfolio/{portfolioId} - Get transactions
**Response:**
```json
[
  {
    "transactionId": "TXN-123456",
    "portfolioId": "PORT-123456",
    "ticker": "AAPL",
    "transactionType": "BUY",
    "quantity": 100,
    "price": 150.00,
    "amount": 15000.00,
    "currency": "USD",
    "status": "COMPLETED",
    "transactionDate": "2025-06-15T10:30:00"
  }
]
```

#### POST /transactions - Create transaction
**Request:**
```json
{
  "portfolioId": "PORT-123456",
  "ticker": "AAPL",
  "transactionType": "BUY",
  "quantity": 50,
  "price": 175.00,
  "amount": 8750.00
}
```

**Transaction Types:** `BUY`, `SELL`, `DEPOSIT`, `WITHDRAWAL`, `DIVIDEND`, `INTEREST`, `FEE`

---

## Application Pages & Components

### Page Structure

```
/                     → Dashboard (default)
/portfolios           → Portfolio List
/portfolios/:id       → Portfolio Detail
/portfolios/:id/assets → Assets Management
/market               → Market Overview / Stock Search
/transactions         → Transaction History
```

---

### 1. DASHBOARD PAGE (`/`)

**Layout:**
- Top row: 3-4 summary cards showing:
  - Total Portfolio Value (with daily change %)
  - Total Gain/Loss (colored green/red)
  - Cash Balance
  - Number of Holdings

- Middle section (2 columns):
  - LEFT: Portfolio Performance Line Chart (show last 30 days)
  - RIGHT: Asset Allocation Donut/Pie Chart

- Bottom section:
  - Holdings Table (top 5-10 assets) with columns:
    - Symbol | Name | Quantity | Price | Value | Gain/Loss % | Allocation %
  - "View All" link to full assets page

**Features:**
- Auto-refresh stock prices every 30 seconds
- Click on portfolio card to navigate to detail
- Responsive grid layout

---

### 2. PORTFOLIO LIST PAGE (`/portfolios`)

**Layout:**
- Header with "My Portfolios" title + "Create Portfolio" button
- Grid of portfolio cards showing:
  - Portfolio name
  - Total value
  - Number of assets
  - Daily change %
  - Last updated date
- Empty state with "Create your first portfolio" CTA

**Actions:**
- Click card → Navigate to portfolio detail
- Create button → Open create modal
- Edit/Delete dropdown menu on each card

---

### 3. PORTFOLIO DETAIL PAGE (`/portfolios/:id`)

**Layout:**
- Breadcrumb: Portfolios > Portfolio Name
- Portfolio header with name, description, edit button
- Summary section (same as dashboard but for single portfolio)
- Tabs: Overview | Holdings | Transactions | Performance

**Holdings Tab:**
- Full assets table with all columns
- Add Asset button
- Edit/Delete actions on each row
- Sort and filter options

**Performance Tab:**
- Line chart with period selector (1D, 1W, 1M, 3M, 1Y, ALL)
- Performance metrics (total return, best/worst day)

---

### 4. MARKET PAGE (`/market`)

**Layout:**
- Stock search bar at top
- Search results dropdown with company name, symbol, exchange
- Watchlist section (saved tickers)
- Real-time quotes table for watched stocks

**Stock Detail Modal:**
- Company logo and name
- Real-time price with change
- Add to portfolio button
- Price chart
- Company info (industry, market cap, website)

---

### 5. ADD ASSET MODAL

**Form Fields:**
- Portfolio selector (dropdown)
- Stock search (autocomplete using /stocks/search)
- Quantity (number input)
- Purchase Price (number input)
- Purchase Date (date picker)
- Notes (optional textarea)
- Asset Type (default: STOCK)

**Validation:**
- All required fields
- Quantity > 0
- Price > 0

---

## UI Components to Create

```
src/
├── components/
│   ├── common/
│   │   ├── Button.jsx
│   │   ├── Card.jsx
│   │   ├── Modal.jsx
│   │   ├── Table.jsx
│   │   ├── Input.jsx
│   │   ├── Select.jsx
│   │   ├── Badge.jsx
│   │   ├── Spinner.jsx
│   │   └── EmptyState.jsx
│   ├── layout/
│   │   ├── Navbar.jsx
│   │   ├── Sidebar.jsx
│   │   └── PageHeader.jsx
│   ├── portfolio/
│   │   ├── PortfolioCard.jsx
│   │   ├── PortfolioSummary.jsx
│   │   ├── PortfolioForm.jsx
│   │   └── PortfolioList.jsx
│   ├── assets/
│   │   ├── AssetTable.jsx
│   │   ├── AssetRow.jsx
│   │   ├── AssetForm.jsx
│   │   └── AddAssetModal.jsx
│   ├── charts/
│   │   ├── PerformanceChart.jsx
│   │   ├── AllocationChart.jsx
│   │   ├── PriceChart.jsx
│   │   └── SparklineChart.jsx
│   └── market/
│       ├── StockSearch.jsx
│       ├── StockQuote.jsx
│       ├── StockCard.jsx
│       └── Watchlist.jsx
├── pages/
│   ├── Dashboard.jsx
│   ├── Portfolios.jsx
│   ├── PortfolioDetail.jsx
│   ├── Market.jsx
│   └── Transactions.jsx
├── services/
│   ├── api.js
│   ├── portfolioService.js
│   ├── assetService.js
│   ├── marketDataService.js
│   └── stockService.js
├── hooks/
│   ├── usePortfolios.js
│   ├── useAssets.js
│   └── useStockQuote.js
└── utils/
    ├── formatters.js
    └── constants.js
```

---

## Styling Guidelines

### Color Palette

```css
/* Primary Colors */
--primary: #1E40AF;        /* Blue - main actions */
--primary-light: #3B82F6;
--primary-dark: #1E3A8A;

/* Status Colors */
--success: #10B981;        /* Green - gains, positive */
--danger: #EF4444;         /* Red - losses, negative */
--warning: #F59E0B;        /* Orange - alerts */

/* Neutral Colors */
--background: #F9FAFB;     /* Page background */
--surface: #FFFFFF;        /* Card background */
--border: #E5E7EB;         /* Borders */
--text-primary: #111827;   /* Main text */
--text-secondary: #6B7280; /* Secondary text */
--text-muted: #9CA3AF;     /* Muted text */
```

### Typography

- Headings: Inter or system-ui, font-weight 600-700
- Body: Inter or system-ui, font-weight 400
- Numbers/Data: Tabular numbers, monospace for alignment

### Component Styling

- Cards: White background, subtle shadow, rounded-lg (8px)
- Buttons: Rounded-md, medium padding, hover states
- Tables: Alternating row colors, hover highlight
- Charts: Match color palette, smooth animations
- Forms: Clear labels, focus states, error messages

### Responsive Breakpoints

```css
sm: 640px   /* Mobile landscape */
md: 768px   /* Tablet */
lg: 1024px  /* Desktop */
xl: 1280px  /* Large desktop */
```

---

## Number Formatting

```javascript
// Currency
const formatCurrency = (value) => 
  new Intl.NumberFormat('en-US', { 
    style: 'currency', 
    currency: 'USD' 
  }).format(value);

// Percentage
const formatPercent = (value) => 
  `${value >= 0 ? '+' : ''}${value.toFixed(2)}%`;

// Large numbers
const formatCompact = (value) =>
  new Intl.NumberFormat('en-US', { 
    notation: 'compact' 
  }).format(value);
```

---

## Key Interactions

1. **Gain/Loss Display:**
   - Positive: Green text with ▲ arrow
   - Negative: Red text with ▼ arrow
   - Zero: Gray text

2. **Loading States:**
   - Skeleton loaders for cards and tables
   - Spinner for buttons during actions
   - Progress bar for page transitions

3. **Error Handling:**
   - Toast notifications for success/error
   - Inline validation messages
   - Empty states with helpful CTAs

4. **Real-time Updates:**
   - Pulse animation when price updates
   - Timestamp showing "Last updated X seconds ago"

---

## Sample Dashboard Layout

```
┌─────────────────────────────────────────────────────────────┐
│  🏦 Portfolio Manager                    [Search] [Profile] │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐        │
│  │ Total Value  │ │ Today's +/-  │ │ Total Gain   │        │
│  │  $218,625    │ │   +$1,245    │ │   +$28,500   │        │
│  │              │ │    +0.57%    │ │    +15.0%    │        │
│  └──────────────┘ └──────────────┘ └──────────────┘        │
│                                                             │
│  ┌─────────────────────────────┐ ┌─────────────────────┐   │
│  │                             │ │                     │   │
│  │   Performance Chart         │ │  Allocation Pie     │   │
│  │   [Line Graph - 30 days]    │ │  [Donut Chart]      │   │
│  │                             │ │                     │   │
│  └─────────────────────────────┘ └─────────────────────┘   │
│                                                             │
│  Holdings                                    [View All →]   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ Symbol │ Name      │ Price   │ Value    │ Gain     │   │
│  │ AAPL   │ Apple     │ $175.50 │ $17,550  │ +17.0%   │   │
│  │ GOOG   │ Alphabet  │ $320.50 │ $16,025  │ +28.2%   │   │
│  │ MSFT   │ Microsoft │ $385.20 │ $28,890  │ +28.4%   │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Important Notes

1. **DO NOT use TypeScript** - Use plain JavaScript (.jsx files)
2. **All API endpoints are already implemented** - Just connect to them
3. **Backend runs on localhost:8080** - Frontend should run on different port (5173)
4. **CORS is configured** - Backend accepts requests from localhost origins
5. **Sample data exists** - Backend has 2 portfolios with 7 assets pre-loaded
6. **Finnhub API integrated** - Real-time stock quotes available via /api/v1/stocks endpoints

---

## Getting Started Checklist

1. Create React + Vite project with JavaScript template
2. Install dependencies: tailwindcss, recharts, axios, react-router-dom, @tanstack/react-query, lucide-react, date-fns
3. Configure Tailwind CSS with the color palette above
4. Set up API service with axios base configuration
5. Create page components with React Router
6. Build reusable UI components
7. Connect to backend APIs
8. Add charts and visualizations
9. Implement real-time price updates
10. Add responsive design

---

**Generate a complete, production-ready React application following these specifications. Match the attached reference image style. Prioritize clean code, good UX, and professional appearance suitable for a financial application.**
