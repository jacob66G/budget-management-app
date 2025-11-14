-- Wyczyszczenie tabel w odpowiedniej kolejności (uwzględniając więzy integralności)
-- Jest to dobra praktyka, choć @DataJpaTest i tak wycofa transakcję.
DELETE FROM transactions;
DELETE FROM recurring_transactions;
DELETE FROM accounts;
DELETE FROM categories;

-- === 3 KATEGORIE ===
INSERT INTO categories (id, name, type, icon_key, is_default, user_id) VALUES
(1, 'Jedzenie', 'EXPENSE', 'icon-food', false, NULL),
(2, 'Wynagrodzenie', 'INCOME', 'icon-salary', false, NULL),
(3, 'Rozrywka', 'EXPENSE', 'icon-entertainment', false, NULL);

-- === 3 KONTA ===
INSERT INTO accounts (id, name, balance, total_income, total_expense, account_status, account_type, currency, is_default, description, budget_type, budget, alert_threshold, created_at, icon_key, include_in_total_balance, user_id) VALUES
(1, 'Główne konto bankowe', 1000.00, 0, 0, 'ACTIVE', 'PERSONAL', 'PLN', true, 'Moje główne konto', 'NONE', NULL, NULL, '2025-01-01 12:00:00', 'icon-bank', true, NULL),
(2, 'Konto oszczędnościowe', 5000.00, 0, 0, 'ACTIVE', 'PERSONAL', 'PLN', false, NULL, 'NONE', NULL, NULL, '2025-01-01 12:01:00', 'icon-savings', true, NULL),
(3, 'Portfel gotówkowy', 300.00, 0, 0, 'ACTIVE', 'PERSONAL', 'PLN', false, 'Gotówka', 'NONE', NULL, NULL, '2025-01-02 10:00:00', 'icon-cash', true, NULL);

-- === 2 SZABLONY REKURENCYJNE ===
INSERT INTO recurring_transactions (id, amount, title, type, description, start_date, end_date, recurring_interval, recurring_value, next_occurrence, is_active, created_at, category_id, account_id) VALUES
(1, 1500.00, 'Czynsz', 'EXPENSE', 'Miesięczny czynsz', '2025-11-01', NULL, 'MONTH', 1, '2025-12-01', true, '2025-11-14 10:00:00', 1, 1),
(2, 54.00, 'Subskrypcja Netflix', 'EXPENSE', 'Opłata za streaming', '2025-11-10', NULL, 'MONTH', 1, '2025-12-10', true, '2025-11-14 10:00:00', 3, 2);

-- === 15 ZWYKŁYCH TRANSAKCJI ===
INSERT INTO transactions (id, amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(1, 150.00, 'Zwykła transakcja 1', 'EXPENSE', NULL, '2025-11-13 12:00:00', 1, 1, NULL),
(2, 45.50, 'Zwykła transakcja 2', 'EXPENSE', NULL, '2025-11-13 14:00:00', 2, 2, NULL),
(3, 5000.00, 'Zwykła transakcja 3', 'INCOME', NULL, '2025-11-12 09:00:00', 3, 3, NULL),
(4, 20.00, 'Zwykła transakcja 4', 'EXPENSE', NULL, '2025-11-12 10:00:00', 1, 1, NULL),
(5, 80.20, 'Zwykła transakcja 5', 'EXPENSE', NULL, '2025-11-11 18:00:00', 2, 2, NULL),
(6, 120.00, 'Zwykła transakcja 6', 'EXPENSE', NULL, '2025-11-10 12:00:00', 3, 3, NULL),
(7, 300.00, 'Zwykła transakcja 7', 'INCOME', NULL, '2025-11-10 13:00:00', 1, 1, NULL),
(8, 15.00, 'Zwykła transakcja 8', 'EXPENSE', NULL, '2025-11-09 15:00:00', 2, 2, NULL),
(9, 200.00, 'Zwykła transakcja 9', 'EXPENSE', NULL, '2025-11-08 16:00:00', 3, 3, NULL),
(10, 55.00, 'Zwykła transakcja 10', 'EXPENSE', NULL, '2025-11-07 12:00:00', 1, 1, NULL),
(11, 1500.00, 'Zwykła transakcja 11', 'INCOME', NULL, '2025-11-06 14:00:00', 2, 2, NULL),
(12, 75.00, 'Zwykła transakcja 12', 'EXPENSE', NULL, '2025-11-05 09:00:00', 3, 3, NULL),
(13, 12.00, 'Zwykła transakcja 13', 'EXPENSE', NULL, '2025-11-04 10:00:00', 1, 1, NULL),
(14, 99.00, 'Zwykła transakcja 14', 'EXPENSE', NULL, '2025-11-03 18:00:00', 2, 2, NULL),
(15, 18.00, 'Zwykła transakcja 15', 'EXPENSE', NULL, '2025-11-02 12:00:00', 1, 3, NULL);

-- === 5 TRANSAKCJI POWIĄZANYCH Z SZABLONEM ===
INSERT INTO transactions (id, amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
-- 3 realizacje dla szablonu 1 (Czynsz)
(16, 1500.00, 'Czynsz (realizacja)', 'EXPENSE', 'Z szablonu', '2025-11-01 10:00:00', 1, 1, 1),
(17, 1500.00, 'Czynsz (realizacja)', 'EXPENSE', 'Z szablonu', '2025-10-01 10:00:00', 1, 1, 1),
(18, 1500.00, 'Czynsz (realizacja)', 'EXPENSE', 'Z szablonu', '2025-09-01 10:00:00', 1, 1, 1),
-- 2 realizacje dla szablonu 2 (Netflix)
(19, 54.00, 'Subskrypcja Netflix (realizacja)', 'EXPENSE', 'Z szablonu', '2025-11-10 11:00:00', 3, 2, 2),
(20, 54.00, 'Subskrypcja Netflix (realizacja)', 'EXPENSE', 'Z szablonu', '2025-10-10 11:00:00', 3, 2, 2);