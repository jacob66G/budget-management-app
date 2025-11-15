-- Wyczyszczenie danych (opcjonalne, na potrzeby testów)
DELETE FROM transactions;
DELETE FROM recurring_transactions;
DELETE FROM categories;
DELETE FROM accounts;
DELETE FROM users;

ALTER TABLE users ALTER COLUMN id RESTART WITH 1;
ALTER TABLE accounts ALTER COLUMN id RESTART WITH 1;
ALTER TABLE categories ALTER COLUMN id RESTART WITH 1;
ALTER TABLE recurring_transactions ALTER COLUMN id RESTART WITH 1;
ALTER TABLE transactions ALTER COLUMN id RESTART WITH 1;
-- ===================================================================================
-- UŻYTKOWNICY (5)
-- ===================================================================================

INSERT INTO users (name, surname, email, password, status, created_at, mfa_enabled, two_factor_secret, temp_two_factor_secret, request_close_at) VALUES
('Jan', 'Kowalski', 'jan.kowalski@example.com', 'pass123', 'ACTIVE', CURRENT_TIMESTAMP, false, NULL, NULL, NULL),
('Anna', 'Nowak', 'anna.nowak@example.com', 'pass123', 'ACTIVE', CURRENT_TIMESTAMP, false, NULL, NULL, NULL),
('Piotr', 'Wiśniewski', 'piotr.wisniewski@example.com', 'pass123', 'ACTIVE', CURRENT_TIMESTAMP, true, 'SZYFR_PIOTR', NULL, NULL),
('Maria', 'Zielińska', 'maria.zielinska@example.com', 'pass123', 'PENDING_CONFIRMATION', CURRENT_TIMESTAMP, false, NULL, NULL, NULL),
('Krzysztof', 'Wójcik', 'krzysztof.wojcik@example.com', 'pass123', 'ACTIVE', CURRENT_TIMESTAMP, false, NULL, NULL, NULL);

-- ===================================================================================
-- KONTA (Kryterium: 2-4 na użytkownika)
-- ===================================================================================

-- Konta dla Usera 1 (3 konta)
INSERT INTO accounts (name, balance, total_income, total_expense, account_status, account_type, currency, is_default, description, budget_type, created_at, include_in_total_balance, user_id) VALUES
('Konto Główne (PLN)', 1500.00, 5000.00, 3500.00, 'ACTIVE', 'PERSONAL', 'PLN', true, 'Główne konto ROR', 'NONE', CURRENT_TIMESTAMP, true, 1),
('Oszczędności (PLN)', 10000.00, 1000.00, 0.00, 'ACTIVE', 'PERSONAL', 'PLN', false, 'Konto oszczędnościowe', 'MONTHLY', CURRENT_TIMESTAMP, true, 1),
('Konto Walutowe (EUR)', 500.00, 500.00, 0.00, 'ACTIVE', 'PERSONAL', 'EUR', false, 'Konto na wydatki w EUR', 'NONE', CURRENT_TIMESTAMP, true, 1);

-- Konta dla Usera 2 (2 konta)
INSERT INTO accounts (name, balance, total_income, total_expense, account_status, account_type, currency, is_default, description, budget_type, created_at, include_in_total_balance, user_id) VALUES
('Portfel (PLN)', 3000, 2000.00, 1699.50, 'ACTIVE', 'PERSONAL', 'PLN', true, 'Gotówka', 'NONE', CURRENT_TIMESTAMP, true, 2),
('Konto USD', 2000.00, 2000.00, 0.00, 'ACTIVE', 'PERSONAL', 'USD', false, NULL, 'NONE', CURRENT_TIMESTAMP, true, 2);

-- Konta dla Usera 3 (4 konta)
INSERT INTO accounts (name, balance, total_income, total_expense, account_status, account_type, currency, is_default, description, budget_type, created_at, include_in_total_balance, user_id) VALUES
('Konto Firmowe (PLN)', 25000.00, 50000.00, 25000.00, 'ACTIVE', 'PERSONAL', 'PLN', true, 'Działalność gospodarcza', 'NONE', CURRENT_TIMESTAMP, true, 3),
('Konto Prywatne (PLN)', 4000.00, 6000.00, 2000.00, 'ACTIVE', 'PERSONAL', 'PLN', false, NULL, 'NONE', CURRENT_TIMESTAMP, true, 3),
('Karta Kredytowa (PLN)', -1500.00, 0.00, 1500.00, 'ACTIVE', 'PERSONAL', 'PLN', false, 'Limit 5000 PLN', 'NONE', CURRENT_TIMESTAMP, true, 3),
('Konto GBP', 800.00, 800.00, 0.00, 'ACTIVE', 'PERSONAL', 'GBP', false, NULL, 'NONE', CURRENT_TIMESTAMP, true, 3);

-- Konta dla Usera 4 (2 konta)
INSERT INTO accounts (name, balance, total_income, total_expense, account_status, account_type, currency, is_default, description, budget_type, created_at, include_in_total_balance, user_id) VALUES
('Konto Główne (EUR)', 3000.00, 4000.00, 1000.00, 'ACTIVE', 'PERSONAL', 'EUR', true, NULL, 'NONE', CURRENT_TIMESTAMP, true, 4),
('Konto Wakacyjne (EUR)', 1200.00, 1200.00, 0.00, 'ACTIVE', 'PERSONAL', 'EUR', false, 'Na wyjazd', 'NONE', CURRENT_TIMESTAMP, true, 4);

-- Konta dla Usera 5 (3 konta)
INSERT INTO accounts (name, balance, total_income, total_expense, account_status, account_type, currency, is_default, description, budget_type, created_at, include_in_total_balance, user_id) VALUES
('Konto Studenckie (PLN)', 500.00, 1500.00, 1000.00, 'ACTIVE', 'PERSONAL', 'PLN', true, NULL, 'NONE', CURRENT_TIMESTAMP, true, 5),
('Revolut (PLN)', 150.00, 300.00, 150.00, 'ACTIVE', 'PERSONAL', 'PLN', false, NULL, 'NONE', CURRENT_TIMESTAMP, true, 5),
('Oszczędności (USD)', 100.00, 100.00, 0.00, 'ACTIVE', 'PERSONAL', 'USD', false, NULL, 'NONE', CURRENT_TIMESTAMP, true, 5);

-- ===================================================================================
-- KATEGORIE (Kryterium: 3-6 na użytkownika)
-- ===================================================================================

-- Kategorie dla Usera 1 (4 kategorie)
INSERT INTO categories (name, type, icon_key, is_default, user_id) VALUES
('Jedzenie', 'EXPENSE', 'icon/food.png', false, 1),
('Transport', 'EXPENSE', 'icon/transport.png', false, 1),
('Wypłata', 'INCOME', 'icon/income.png', false, 1),
('Rachunki', 'EXPENSE', 'icon/bills.png', false, 1);

-- Kategorie dla Usera 2 (3 kategorie)
INSERT INTO categories (name, type, icon_key, is_default, user_id) VALUES
('Rozrywka', 'EXPENSE', 'icon/entertainment.png', false, 2),
('Mieszkanie', 'EXPENSE', 'icon/home.png', false, 2),
('Premia', 'INCOME', 'icon/bonus.png', false, 2);

-- Kategorie dla Usera 3 (5 kategorii)
INSERT INTO categories (name, type, icon_key, is_default, user_id) VALUES
('Podatki', 'EXPENSE', 'icon/tax.png', false, 3),
('Faktury (Przychód)', 'INCOME', 'icon/invoice.png', false, 3),
('Restauracje', 'EXPENSE', 'icon/restaurant.png', false, 3),
('Samochód', 'EXPENSE', 'icon/car.png', false, 3),
('Inne (Wydatek)', 'EXPENSE', 'icon/other.png', true, 3);

-- Kategorie dla Usera 4 (6 kategorii)
INSERT INTO categories (name, type, icon_key, is_default, user_id) VALUES
('Zakupy (Ubrania)', 'EXPENSE', 'icon/clothes.png', false, 4),
('Prezenty', 'EXPENSE', 'icon/gifts.png', false, 4),
('Zdrowie i Uroda', 'EXPENSE', 'icon/health.png', false, 4),
('Pensja', 'INCOME', 'icon/income.png', false, 4),
('Sport', 'EXPENSE', 'icon/sport.png', false, 4),
('Dzieci', 'EXPENSE', 'icon/children.png', false, 4);

-- Kategorie dla Usera 5 (3 kategorie)
INSERT INTO categories (name, type, icon_key, is_default, user_id) VALUES
('Czesne', 'EXPENSE', 'icon/education.png', false, 5),
('Stypendium', 'INCOME', 'icon/scholarship.png', false, 5),
('Jedzenie na mieście', 'EXPENSE', 'icon/food_outside.png', false, 5);

-- ===================================================================================
-- SZABLONY TRANSAKCJI REKURENCYJNYCH (10)
-- ===================================================================================

-- Szablony dla Usera 1 (Konta: 1-3, Kategorie: 1-4)
INSERT INTO recurring_transactions (amount, title, type, description, start_date, end_date, recurring_interval, recurring_value, next_occurrence, is_active, created_at, category_id, account_id) VALUES
(60.00, 'Netflix', 'EXPENSE', 'Abonament Netflix', '2024-01-05', NULL, 'MONTH', 1, '2024-11-05', true, CURRENT_TIMESTAMP, 4, 1),
(1500.00, 'Czynsz', 'EXPENSE', 'Opłata za mieszkanie', '2024-01-10', '2025-12-31', 'MONTH', 1, '2024-11-10', true, CURRENT_TIMESTAMP, 4, 1);

-- Szablony dla Usera 2 (Konta: 4-5, Kategorie: 5-7)
INSERT INTO recurring_transactions (amount, title, type, description, start_date, end_date, recurring_interval, recurring_value, next_occurrence, is_active, created_at, category_id, account_id) VALUES
(120.00, 'Siłownia', 'EXPENSE', NULL, '2024-01-15', NULL, 'MONTH', 1, '2024-11-15', true, DATEADD('DAY', -10, CURRENT_TIMESTAMP), 5, 4),
(50.00, 'Telefon', 'EXPENSE', 'Abonament komórkowy', '2024-01-01', NULL, 'MONTH', 1, '2024-11-01', true, CURRENT_TIMESTAMP, 6, 4);

-- Szablony dla Usera 3 (Konta: 6-9, Kategorie: 8-12)
INSERT INTO recurring_transactions (amount, title, type, description, start_date, end_date, recurring_interval, recurring_value, next_occurrence, is_active, created_at, category_id, account_id) VALUES
(1800.00, 'ZUS', 'EXPENSE', 'Składka ZUS', '2024-01-20', NULL, 'MONTH', 1, '2024-11-20', true, CURRENT_TIMESTAMP, 8, 6),
(1200.00, 'Leasing', 'EXPENSE', 'Rata leasingowa za auto', '2024-01-15', '2026-01-15', 'MONTH', 1, '2025-08-15', false, CURRENT_TIMESTAMP, 11, 6);

-- Szablony dla Usera 4 (Konta: 10-11, Kategorie: 13-18)
INSERT INTO recurring_transactions (amount, title, type, description, start_date, end_date, recurring_interval, recurring_value, next_occurrence, is_active, created_at, category_id, account_id) VALUES
(200.00, 'Oszczędności na wakacje', 'EXPENSE', 'Przelew na konto wakacyjne', '2024-02-01', NULL, 'MONTH', 1, '2024-11-01', true, CURRENT_TIMESTAMP, 18, 10),
(50.00, 'Ubezpieczenie zdrowotne', 'EXPENSE', NULL, '2024-01-10', NULL, 'MONTH', 1, '2024-11-10', false, CURRENT_TIMESTAMP, 15, 10);

-- Szablony dla Usera 5 (Konta: 12-14, Kategorie: 19-21)
INSERT INTO recurring_transactions (amount, title, type, description, start_date, end_date, recurring_interval, recurring_value, next_occurrence, is_active, created_at, category_id, account_id) VALUES
(800.00, 'Stypendium naukowe', 'INCOME', 'Wpływ z uczelni', '2024-10-15', '2025-06-15', 'MONTH', 1, '2024-11-15', true, CURRENT_TIMESTAMP, 20, 12),
(500.00, 'Czesne za studia', 'EXPENSE', 'Opłata semestralna', '2024-10-01', '2026-06-01', 'MONTH', 1, '2024-11-01', true, CURRENT_TIMESTAMP, 19, 12),
(50.00, 'Karnet', 'EXPENSE', NULL, '2024-01-01', NULL, 'MONTH', 1, CURRENT_DATE, true, CURRENT_TIMESTAMP, 19, 12),
(50.00, 'Karnecik', 'EXPENSE', NULL, '2025-10-31', NULL, 'MONTH', 1, CURRENT_DATE, false, CURRENT_TIMESTAMP, 19, 12),
(50.00, 'Karnet na siłownie', 'EXPENSE', NULL, '2025-06-06', NULL, 'MONTH', 1, '2025-08-06', false, CURRENT_TIMESTAMP, 19, 12),
(50.00, 'Karnet na siłownie', 'EXPENSE', NULL, '2024-06-01', NULL, 'MONTH', 1, CURRENT_DATE - INTERVAL '4' MONTH, false, CURRENT_TIMESTAMP, 19, 12),
(50.00, 'Karnet na siłownie', 'EXPENSE', NULL, '2025-06-06', NULL, 'MONTH', 1, CURRENT_DATE - INTERVAL '3' MONTH, false, CURRENT_TIMESTAMP, 19, 12),
-- user 1, konta 1-3
(150.00, 'Siłownia', 'EXPENSE', NULL, '2024-01-10', '2025-12-31', 'MONTH', 1, CURRENT_DATE, true, CURRENT_TIMESTAMP, 4, 1),
(50.00, 'HBO', 'EXPENSE', NULL, '2024-01-10', '2025-12-31', 'MONTH', 1, CURRENT_DATE + INTERVAL '2' DAY, true, CURRENT_TIMESTAMP, 4, 1),
(5000.00, 'Wypłata', 'INCOME', NULL, '2024-01-10', '2025-12-31', 'MONTH', 1, CURRENT_DATE + INTERVAL '7' DAY, true, CURRENT_TIMESTAMP, 3, 2),
(150.00, 'Fitness', 'EXPENSE', NULL, '2024-01-10', '2025-12-31', 'MONTH', 1, CURRENT_DATE + INTERVAL '13' DAY, true, CURRENT_TIMESTAMP, 4, 1),
(75.00, 'Bilet miesięczny', 'EXPENSE', NULL, '2024-01-10', '2025-12-31', 'MONTH', 1, CURRENT_DATE + INTERVAL '10' DAY, true, CURRENT_TIMESTAMP, 4, 1),
(75.00, 'Internet', 'EXPENSE', NULL, '2024-01-10', '2025-12-31', 'MONTH', 1, CURRENT_DATE + INTERVAL '20' DAY, true, CURRENT_TIMESTAMP, 4, 1),
(200.00, 'Premia', 'INCOME', NULL, '2024-01-10', '2025-12-31', 'MONTH', 1, CURRENT_DATE + INTERVAL '21' DAY, true, CURRENT_TIMESTAMP, 3, 2);


-- ===================================================================================
-- TRANSAKCJE POWIĄZANE Z SZABLONAMI (20)
-- (Po 2 transakcje na każdy z 10 szablonów)
-- ===================================================================================

-- Transakcje dla Szablonu 1 (Netflix, User 1)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(60.00, 'Netflix', 'EXPENSE', 'Abonament Netflix', DATEADD('MONTH', -1, DATEADD('DAY', -10, CURRENT_TIMESTAMP)), 4, 1, 1),
(60.00, 'Netflix', 'EXPENSE', 'Abonament Netflix', DATEADD('DAY', -10, CURRENT_TIMESTAMP), 4, 1, 1);

-- Transakcje dla Szablonu 2 (Czynsz, User 1)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(1500.00, 'Czynsz', 'EXPENSE', 'Opłata za mieszkanie', DATEADD('MONTH', -1, DATEADD('DAY', -5, CURRENT_TIMESTAMP)), 4, 1, 2),
(1500.00, 'Czynsz', 'EXPENSE', 'Opłata za mieszkanie', DATEADD('DAY', -5, CURRENT_TIMESTAMP), 4, 1, 2);

-- Transakcje dla Szablonu 3 (Siłownia, User 2)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(120.00, 'Siłownia', 'EXPENSE', NULL, DATEADD('MONTH', -1, DATEADD('DAY', -20, CURRENT_TIMESTAMP)), 5, 4, 3),
(120.00, 'Siłownia', 'EXPENSE', NULL, DATEADD('DAY', -20, CURRENT_TIMESTAMP), 5, 4, 3);

-- Transakcje dla Szablonu 4 (Telefon, User 2)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(50.00, 'Telefon', 'EXPENSE', 'Abonament komórkowy', DATEADD('MONTH', -1, DATEADD('DAY', -1, CURRENT_TIMESTAMP)), 6, 4, 4),
(50.00, 'Telefon', 'EXPENSE', 'Abonament komórkowy', DATEADD('DAY', -1, CURRENT_TIMESTAMP), 6, 4, 4);

-- Transakcje dla Szablonu 5 (ZUS, User 3)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(1800.00, 'ZUS', 'EXPENSE', 'Składka ZUS', DATEADD('MONTH', -1, DATEADD('DAY', -10, CURRENT_TIMESTAMP)), 8, 6, 5),
(1800.00, 'ZUS', 'EXPENSE', 'Składka ZUS', DATEADD('DAY', -10, CURRENT_TIMESTAMP), 8, 6, 5);

-- Transakcje dla Szablonu 6 (Leasing, User 3)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(1200.00, 'Leasing', 'EXPENSE', 'Rata leasingowa za auto', DATEADD('MONTH', -1, DATEADD('DAY', -12, CURRENT_TIMESTAMP)), 11, 6, 6),
(1200.00, 'Leasing', 'EXPENSE', 'Rata leasingowa za auto', DATEADD('DAY', -12, CURRENT_TIMESTAMP), 11, 6, 6);

-- Transakcje dla Szablonu 7 (Oszczędności, User 4)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(200.00, 'Oszczędności na wakacje', 'EXPENSE', 'Przelew na konto wakacyjne', DATEADD('MONTH', -1, DATEADD('DAY', -3, CURRENT_TIMESTAMP)), 18, 10, 7),
(200.00, 'Oszczędności na wakacje', 'EXPENSE', 'Przelew na konto wakacyjne', DATEADD('DAY', -3, CURRENT_TIMESTAMP), 18, 10, 7);

-- Transakcje dla Szablonu 8 (Ubezpieczenie, User 4 - szablon nieaktywny, ale transakcje mogły być)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(50.00, 'Ubezpieczenie zdrowotne', 'EXPENSE', NULL, DATEADD('MONTH', -1, DATEADD('DAY', -22, CURRENT_TIMESTAMP)), 15, 10, 8),
(50.00, 'Ubezpieczenie zdrowotne', 'EXPENSE', NULL, DATEADD('DAY', -22, CURRENT_TIMESTAMP), 15, 10, 8);

-- Transakcje dla Szablonu 9 (Stypendium, User 5)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(800.00, 'Stypendium naukowe', 'INCOME', 'Wpływ z uczelni', DATEADD('MONTH', -1, DATEADD('DAY', -5, CURRENT_TIMESTAMP)), 20, 12, 9),
(800.00, 'Stypendium naukowe', 'INCOME', 'Wpływ z uczelni', DATEADD('DAY', -5, CURRENT_TIMESTAMP), 20, 12, 9);

-- Transakcje dla Szablonu 10 (Czesne, User 5)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(500.00, 'Czesne za studia', 'EXPENSE', 'Opłata semestralna', DATEADD('MONTH', -1, DATEADD('DAY', -7, CURRENT_TIMESTAMP)), 19, 12, 10),
(500.00, 'Czesne za studia', 'EXPENSE', 'Opłata semestralna', DATEADD('DAY', -7, CURRENT_TIMESTAMP), 19, 12, 10);

-- ===================================================================================
-- TRANSAKCJE ZWYKŁE (NIEPOWIĄZANE Z SZABLONAMI) (40)
-- (ID zaczynają się od 21, recurring_transaction_id = NULL)
-- ===================================================================================

-- Transakcje dla Usera 1 (8 transakcji)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(120.50, 'Zakupy spożywcze', 'EXPENSE', 'Biedronka', DATEADD('HOUR', -1, CURRENT_TIMESTAMP), 1, 1, NULL),
(50.00, 'Bilet miesięczny', 'EXPENSE', NULL, DATEADD('DAY', -15, CURRENT_TIMESTAMP), 2, 1, NULL),
(5000.00, 'Wypłata', 'INCOME', 'Wynagrodzenie za Październik', DATEADD('DAY', -2, CURRENT_TIMESTAMP), 3, 1, NULL),
(200.00, 'Paliwo', 'EXPENSE', 'Orlen', DATEADD('DAY', -7, CURRENT_TIMESTAMP), 2, 1, NULL),
(70.00, 'Kino', 'EXPENSE', 'Cinema City', DATEADD('MONTH', -1, CURRENT_TIMESTAMP), 1, 1, NULL),
(1000.00, 'Przelew na oszczędności', 'EXPENSE', NULL, DATEADD('DAY', -2, CURRENT_TIMESTAMP), 4, 1, NULL),
(30.00, 'Restauracja (EUR)', 'EXPENSE', 'Obiad w Berlinie', DATEADD('MONTH', -2, CURRENT_TIMESTAMP), 1, 3, NULL),
(1000.00, 'Wpłata na oszczędności', 'INCOME', NULL, DATEADD('DAY', -2, CURRENT_TIMESTAMP), 3, 2, NULL);

-- Transakcje dla Usera 2 (8 transakcji)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(300.00, 'Bilet na koncert', 'EXPENSE', 'Coldplay', DATEADD('DAY', -5, CURRENT_TIMESTAMP), 5, 4, NULL),
(200.00, 'Prąd', 'EXPENSE', 'Rachunek za prąd', DATEADD('HOUR', -7, CURRENT_TIMESTAMP), 6, 4, NULL),
(80.00, 'Gaz', 'EXPENSE', 'Rachunek za gaz', DATEADD('DAY', -1, CURRENT_TIMESTAMP), 6, 4, NULL),
(1000.00, 'Premia', 'INCOME', 'Premia kwartalna', DATEADD('MONTH', -2, CURRENT_TIMESTAMP), 7, 4, NULL),
(45.00, 'Pyszne.pl', 'EXPENSE', 'Pizza', DATEADD('DAY', -6, CURRENT_TIMESTAMP), 5, 4, NULL),
(500.00, 'Wpłata na konto USD', 'INCOME', 'Przewalutowanie', DATEADD('DAY', -2, CURRENT_TIMESTAMP), 7, 5, NULL),
(25.50, 'Uber', 'EXPENSE', NULL, DATEADD('DAY', -25, CURRENT_TIMESTAMP), 5, 4, NULL),
(60.00, 'Internet', 'EXPENSE', 'Opłata za internet', DATEADD('MONTH', -3, CURRENT_TIMESTAMP), 6, 4, NULL);

-- Transakcje dla Usera 3 (8 transakcji)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(250.00, 'Obiad biznesowy', 'EXPENSE', 'Restauracja Sowa', DATEADD('MONTH', -2, CURRENT_TIMESTAMP), 10, 6, NULL),
(15000.00, 'Faktura 10/2024', 'INCOME', 'Klient X', DATEADD('DAY', -3, CURRENT_TIMESTAMP), 9, 6, NULL),
(10000.00, 'Faktura 11/2024', 'INCOME', 'Klient Y', DATEADD('MONTH', -1, CURRENT_TIMESTAMP), 9, 6, NULL),
(150.00, 'Myjnia', 'EXPENSE', 'Myjnia ręczna', DATEADD('MONTH', -3, CURRENT_TIMESTAMP), 11, 7, NULL),
(1500.00, 'Spłata karty kredytowej', 'EXPENSE', NULL, DATEADD('DAY', -5, CURRENT_TIMESTAMP), 12, 7, NULL),
(300.00, 'Zakupy na Allegro', 'EXPENSE', 'Części do komputera', DATEADD('DAY', -14, CURRENT_TIMESTAMP), 12, 8, NULL),
(150.00, 'Książki', 'EXPENSE', 'Księgarnia techniczna', DATEADD('DAY', -30, CURRENT_TIMESTAMP), 12, 7, NULL),
(100.00, 'Hotel (GBP)', 'EXPENSE', 'Pobyt w Londynie', DATEADD('HOUR', -3, CURRENT_TIMESTAMP), 10, 9, NULL);

-- Transakcje dla Usera 4 (8 transakcji)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(4000.00, 'Pensja (EUR)', 'INCOME', 'Wynagrodzenie 10/2024', DATEADD('DAY', -24, CURRENT_TIMESTAMP), 16, 10, NULL),
(150.00, 'Zara', 'EXPENSE', 'Nowa kurtka', DATEADD('MONTH', -2, CURRENT_TIMESTAMP), 13, 10, NULL),
(50.00, 'Wizyta u lekarza (EUR)', 'EXPENSE', 'Internista', DATEADD('DAY', -2, CURRENT_TIMESTAMP), 15, 10, NULL),
(40.00, 'Prezent urodzinowy', 'EXPENSE', 'Dla Anny', DATEADD('DAY', -22, CURRENT_TIMESTAMP), 14, 10, NULL),
(80.00, 'Zabawki', 'EXPENSE', 'Lego', DATEADD('DAY', -4, CURRENT_TIMESTAMP), 18, 10, NULL),
(60.00, 'Bilet do muzeum', 'EXPENSE', NULL, DATEADD('DAY', -12, CURRENT_TIMESTAMP), 17, 10, NULL),
(1000.00, 'Przelew na konto wakacyjne', 'INCOME', 'Zasilenie', DATEADD('MONTH', -1, CURRENT_TIMESTAMP), 16, 11, NULL),
(25.00, 'Apteka (EUR)', 'EXPENSE', 'Leki', DATEADD('DAY', -3, CURRENT_TIMESTAMP), 15, 10, NULL);

-- Transakcje dla Usera 5 (8 transakcji)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(15.00, 'Kawa', 'EXPENSE', 'Starbucks', DATEADD('DAY', -2, CURRENT_TIMESTAMP), 21, 12, NULL),
(30.00, 'Obiad na stołówce', 'EXPENSE', NULL, DATEADD('DAY', -8, CURRENT_TIMESTAMP), 21, 12, NULL),
(300.00, 'Praca dorywcza', 'INCOME', 'Zlecenie graficzne', DATEADD('MONTH', -1, CURRENT_TIMESTAMP), 20, 13, NULL),
(45.00, 'Bilet na pociąg', 'EXPENSE', 'PKP Intercity', DATEADD('HOUR', -2, CURRENT_TIMESTAMP), 21, 12, NULL),
(150.00, 'Książki na studia', 'EXPENSE', 'Ksero', DATEADD('DAY', -22, CURRENT_TIMESTAMP), 19, 12, NULL),
(20.00, 'Amazon (USD)', 'EXPENSE', 'Ebook', DATEADD('HOUR', -1, CURRENT_TIMESTAMP), 19, 14, NULL),
(100.00, 'Wypłata z bankomatu', 'EXPENSE', 'Prowizja', DATEADD('DAY', -14, CURRENT_TIMESTAMP), 21, 13, NULL),
(25.00, 'Kebab', 'EXPENSE', NULL, DATEADD('DAY', -20, CURRENT_TIMESTAMP), 21, 12, NULL);