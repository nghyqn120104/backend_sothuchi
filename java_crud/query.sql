-- Bảng users
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50),
    password VARCHAR(100),
    email VARCHAR(100)
);

-- Bảng transactions
CREATE TABLE transactions (
    id VARCHAR(36) PRIMARY KEY,
    description VARCHAR(255),
    amount DOUBLE,
    date DATE,
    type VARCHAR(20),
    category VARCHAR(50),
    user_id VARCHAR(36),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Bảng budgets
CREATE TABLE budgets (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36),
    category VARCHAR(50),
    amount DOUBLE,
    month INT,
    year INT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Bảng budget_alerts
CREATE TABLE budget_alerts (
    id VARCHAR(36) PRIMARY KEY,
    budget_id VARCHAR(36),
    user_id VARCHAR(36),
    month INT,
    year INT,
    category VARCHAR(50),
    alert_date DATE,
    still_exceeded BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (budget_id) REFERENCES budgets(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

ALTER TABLE budgets
ADD CONSTRAINT uq_user_category_month_year
UNIQUE (user_id, category, month, year);

CREATE TABLE accounts (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50),
    balance DOUBLE DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Nếu bảng đã tạo, bạn phải DROP FK cũ trước rồi thêm lại
ALTER TABLE accounts
DROP CONSTRAINT IF EXISTS accounts_user_id_fkey;

ALTER TABLE accounts
ADD CONSTRAINT accounts_user_id_fkey
FOREIGN KEY (user_id)
REFERENCES users(id)
ON DELETE CASCADE;

ALTER TABLE transactions
ADD COLUMN account_id VARCHAR(36);

ALTER TABLE transactions
ADD CONSTRAINT fk_transaction_account
FOREIGN KEY (account_id)
REFERENCES accounts(id)
ON DELETE SET NULL
ON UPDATE CASCADE;

CREATE INDEX idx_transaction_account_id
ON transactions(account_id);

ALTER TABLE accounts
ADD CONSTRAINT uq_user_account_name UNIQUE (user_id, name);

CREATE TABLE saving_goals (
    id              VARCHAR(36) PRIMARY KEY,
    user_id         VARCHAR(36) NOT NULL,
    name            VARCHAR(100),
    target_amount   DOUBLE,
    start_date      DATE,
    end_date        DATE,
    account_id      VARCHAR(36),

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES accounts(id)
);
-- Dùng cho chạy MySQL

