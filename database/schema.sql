-- ============================================
-- Smart Expense Manager - PostgreSQL Schema
-- ============================================

CREATE DATABASE expense_db;
\c expense_db;

-- Users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT NOW()
);

-- Expenses
CREATE TABLE expenses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount DECIMAL(12, 2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description TEXT,
    raw_input TEXT,
    expense_type VARCHAR(10) CHECK (expense_type IN ('NEED', 'WANT')),
    expense_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Budgets
CREATE TABLE budgets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category VARCHAR(50),           -- NULL = overall budget
    daily_limit DECIMAL(12, 2),
    monthly_limit DECIMAL(12, 2),
    month INT NOT NULL CHECK (month BETWEEN 1 AND 12),
    year INT NOT NULL,
    UNIQUE (user_id, month, year, category)
);

-- Notifications
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    type VARCHAR(30),               -- DAILY_LIMIT, MONTHLY_LIMIT, SUGGESTION
    read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX idx_expenses_user_date ON expenses(user_id, expense_date);
CREATE INDEX idx_expenses_user_month ON expenses(user_id, EXTRACT(MONTH FROM expense_date), EXTRACT(YEAR FROM expense_date));
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, read);
