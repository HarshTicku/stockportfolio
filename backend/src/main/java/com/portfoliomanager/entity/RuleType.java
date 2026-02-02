package com.portfoliomanager.entity;

public enum RuleType {
    AMOUNT_THRESHOLD,    // Alert when transaction exceeds amount
    VELOCITY,            // Alert on rapid transactions
    PRICE_CHANGE,        // Alert on significant price change
    PORTFOLIO_VALUE,     // Alert on portfolio value threshold
    DAILY_LIMIT          // Alert on daily transaction limit
}
