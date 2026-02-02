# Portfolio Manager - Backend

A Spring Boot REST API for managing financial portfolios, assets, and market data.

## Technology Stack

- **Framework**: Spring Boot 3.2
- **Language**: Java 17
- **Database**: H2 (development) / MySQL (production)
- **ORM**: Spring Data JPA
- **API Docs**: Springdoc OpenAPI (Swagger)
- **Build**: Maven

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Run the Application

```bash
# Navigate to backend directory
cd backend

# Build the project
mvn clean install

# Run with H2 database (development)
mvn spring-boot:run

# OR run with MySQL (production)
mvn spring-boot:run -Dspring.profiles.active=mysql
```

### Access Points

| Service | URL |
|---------|-----|
| API Base | http://localhost:8080/api/v1 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| H2 Console | http://localhost:8080/h2-console |
| API Docs | http://localhost:8080/v3/api-docs |

## API Endpoints

### Portfolios
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/portfolios` | Create portfolio |
| GET | `/api/v1/portfolios` | List all portfolios |
| GET | `/api/v1/portfolios/{id}` | Get portfolio |
| GET | `/api/v1/portfolios/{id}/summary` | Get portfolio summary |
| PUT | `/api/v1/portfolios/{id}` | Update portfolio |
| DELETE | `/api/v1/portfolios/{id}` | Delete portfolio |

### Assets
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/assets` | Create asset |
| GET | `/api/v1/assets` | List all assets |
| GET | `/api/v1/assets/{id}` | Get asset |
| GET | `/api/v1/assets/portfolio/{portfolioId}` | Get assets by portfolio |
| PUT | `/api/v1/assets/{id}` | Update asset |
| DELETE | `/api/v1/assets/{id}` | Delete asset |

### Market Data
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/market-data/{ticker}/latest` | Get latest price |
| GET | `/api/v1/market-data/{ticker}/history` | Get price history |
| GET | `/api/v1/market-data/tickers` | List available tickers |
| POST | `/api/v1/market-data` | Add market data |

### Transactions
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/transactions` | Create transaction |
| GET | `/api/v1/transactions` | List all transactions |
| GET | `/api/v1/transactions/{id}` | Get transaction |
| GET | `/api/v1/transactions/portfolio/{portfolioId}` | Get by portfolio |
| DELETE | `/api/v1/transactions/{id}` | Delete transaction |

## Sample Data

The application loads sample data on startup (H2 profile only):
- 2 sample portfolios
- 7 assets across portfolios
- Historical market data for AAPL, GOOG, MSFT, AMZN, META, NFLX

## Database Configuration

### H2 (Development - Default)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:portfoliodb
    username: sa
    password: 
```

### MySQL (Production)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/portfolio_manager
    username: portfolio_user
    password: Portfolio@123
```

## Project Structure

```
src/main/java/com/portfoliomanager/
├── PortfolioManagerApplication.java
├── config/           # Configuration classes
├── controller/       # REST controllers
├── dto/              # Data transfer objects
├── entity/           # JPA entities
├── exception/        # Exception handling
├── repository/       # Data access layer
└── service/          # Business logic
```

## Testing

```bash
# Run tests
mvn test

# Run with coverage
mvn test jacoco:report
```
