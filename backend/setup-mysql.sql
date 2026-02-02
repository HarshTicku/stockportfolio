-- Portfolio Manager MySQL Setup Script
-- Run this script as MySQL root user:
-- mysql -u root -p < setup-mysql.sql

-- Create database
CREATE DATABASE IF NOT EXISTS portfolio_manager
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Create user (if not exists)
CREATE USER IF NOT EXISTS 'portfolio_user'@'localhost' IDENTIFIED BY 'Portfolio@123';

-- Grant privileges
GRANT ALL PRIVILEGES ON portfolio_manager.* TO 'portfolio_user'@'localhost';
FLUSH PRIVILEGES;

-- Verify setup
SELECT 'Database setup complete!' AS status;
SHOW DATABASES LIKE 'portfolio_manager';
