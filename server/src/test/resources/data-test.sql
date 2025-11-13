
-- 1. USERS
-- All user passwords: 'password123' (Bcrypt hash $2a$10$f/9e.11i..k2A7/r0cQhA.i60.j1q9.hV4F.tT2.g3T1K2.uY2.e)
INSERT INTO users (id, name, surname, email, password, status, created_at, mfa_enabled, two_factor_secret, temp_two_factor_secret, request_close_at)
VALUES
    (1, 'John', 'Smith', 'john.smith@example.com', '$2a$10$f/9e.11i..k2A7/r0cQhA.i60.j1q9.hV4F.tT2.g3T1K2.uY2.e', 'ACTIVE', TIMESTAMP '2025-01-01 10:00:00', false, NULL, NULL, NULL),
    (2, 'Anna', 'Novak', 'anna.novak@example.com', '$2a$10$f/9e.11i..k2A7/r0cQhA.i60.j1q9.hV4F.tT2.g3T1K2.uY2.e', 'ACTIVE', TIMESTAMP '2025-01-02 10:00:00', false, NULL, NULL, NULL),
    (3, 'Peter', 'Green', 'peter.green@example.com', '$2a$10$f/9e.11i..k2A7/r0cQhA.i60.j1q9.hV4F.tT2.g3T1K2.uY2.e', 'ACTIVE', TIMESTAMP '2025-01-03 10:00:00', false, NULL, NULL, NULL),
    (4, 'Eve', 'Malin', 'eve.malin@example.com', '$2a$10$f/9e.11i..k2A7/r0cQhA.i60.j1q9.hV4F.tT2.g3T1K2.uY2.e', 'PENDING_CONFIRMATION', TIMESTAMP '2025-01-04 10:00:00', false, NULL, NULL, NULL),
    (5, 'Kamil', 'Wojcik', 'kamil.wojcik@example.com', '$2a$10$f/9e.11i..k2A7/r0cQhA.i60.j1q9.hV4F.tT2.g3T1K2.uY2.e', 'ACTIVE', TIMESTAMP '2025-01-05 10:00:00', false, NULL, NULL, NULL);

-- 2. CATEGORIES
INSERT INTO categories (name, type, icon_key, is_default, user_id)
VALUES
    -- User 1
    ('Salary', 'INCOME', '/icons/cat/salary.png', false, 1),
    ('Food & Groceries', 'EXPENSE', '/icons/cat/food.png', false, 1),
    ('Other', 'INCOME', '/icons/cat/other.png', true, 1),
    ('Other', 'EXPENSE', '/icons/cat/other.png', true, 1),
    ('Other', 'GENERAL', '/icons/cat/other.png', true, 1),
    -- User 2
    ('Salary', 'INCOME', '/icons/cat/salary.png', false, 2),
    ('Food & Groceries', 'EXPENSE', '/icons/cat/food.png', false, 2),
    ('Other', 'INCOME', '/icons/cat/other.png', true, 2),
    ('Other', 'EXPENSE', '/icons/cat/other.png', true, 2),
    ('Other', 'GENERAL', '/icons/cat/other.png', true, 2),
    -- User 3
    ('Other', 'INCOME', '/icons/cat/other.png', true, 3),
    ('Other', 'EXPENSE', '/icons/cat/other.png', true, 3),
    ('Other', 'GENERAL', '/icons/cat/other.png', true, 3),
    -- User 5
    ('Other', 'INCOME', '/icons/cat/other.png', true, 5),
    ('Other', 'EXPENSE', '/icons/cat/other.png', true, 5),
    ('Other', 'GENERAL', '/icons/cat/other.png', true, 5);


-- 3. ACCOUNTS
INSERT INTO accounts (id, name, balance, total_income, total_expense, account_status, account_type, currency, is_default, description, budget_type, budget, alert_treshold, created_at, icon_key, include_in_total_balance, user_id)
VALUES
    -- User 1
    (1, 'Main Wallet', 1500.50, 5000.00, 3500.50, 'ACTIVE', 'PERSONAL', 'PLN', true, 'Main personal account', 'MONTHLY', 3000.00, 0.8, TIMESTAMP '2025-02-15 10:00:00', '/icons/acc/wallet.png', true, 1),
    (2, 'Savings', 10000.00, 200.00, 0.00, 'ACTIVE', 'PERSONAL', 'PLN', false, 'Savings for vacation', 'NONE', NULL, NULL, TIMESTAMP '2025-01-10 10:00:00', '/icons/acc/savings.png', true, 1),
    (3, 'USD Account', 500.00, 500.00, 0.00, 'ACTIVE', 'PERSONAL', 'USD', false, 'For expenses in USD', 'NONE', NULL, NULL, TIMESTAMP '2025-02-01 10:00:00', '/icons/acc/dollar.png', true, 1),
    (8, 'Vacation Account', 2000.00, 2000.00, 0.00, 'ACTIVE', 'PERSONAL', 'PLN', false, 'Additional account', 'NONE', NULL, NULL, TIMESTAMP '2025-03-01 10:00:00', '/icons/acc/holidays.png', false, 1),

    -- User 2
    (4, 'Current Account', 2500.00, 6000.00, 3500.00, 'ACTIVE', 'PERSONAL', 'PLN', true, 'Daily expenses', 'MONTHLY', 4000.00, 0.9, TIMESTAMP '2025-01-20 10:00:00', '/icons/acc/card.png', true, 2),
    (5, 'Credit Card', -800.00, 0.00, 800.00, 'ACTIVE', 'PERSONAL', 'PLN', false, 'Credit card for bills', 'NONE', NULL, NULL, TIMESTAMP '2025-01-21 10:00:00', '/icons/acc/credit.png', false, 2),

    -- User 3
    (6, 'EUR Wallet', 1200.00, 1200.00, 0.00, 'ACTIVE', 'PERSONAL', 'EUR', true, 'Account in Euro', 'NONE', NULL, NULL, TIMESTAMP '2025-01-25 10:00:00', '/icons/acc/euro.png', true, 3),

    -- User 5
    (7, 'Old Wallet', 500.00, 500.00, 0.00, 'INACTIVE', 'PERSONAL', 'PLN', true, 'Inactive wallet', 'NONE', NULL, NULL, TIMESTAMP '2025-01-05 10:00:00', '/icons/acc/wallet_inactive.png', false, 5);

