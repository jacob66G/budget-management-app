-- Wyczyszczenie danych (opcjonalne, na potrzeby testów)
DELETE FROM transactions;
DELETE FROM recurring_transactions;
DELETE FROM categories;
DELETE FROM accounts;
DELETE FROM users;

-- ===================================================================================
-- UŻYTKOWNICY (5)
-- ===================================================================================

INSERT INTO users (name, surname, email, password, status, email_last_changed, created_at, mfa_enabled) VALUES
('Jan', 'Kowalski', 'jan.kowalski@example.com', 'pass123', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('Anna', 'Nowak', 'anna.nowak@example.com', 'pass123', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('Piotr', 'Wiśniewski', 'piotr.wisniewski@example.com', 'pass123', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
('Maria', 'Zielińska', 'maria.zielinska@example.com', 'pass123', 'PENDING_CONFIRMATION', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('Krzysztof', 'Wójcik', 'krzysztof.wojcik@example.com', 'pass123', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false);

-- ===================================================================================
-- KONTA (Kryterium: 2-4 na użytkownika)
-- ===================================================================================

-- Konta dla Usera 1 (3 konta)
INSERT INTO accounts (name, balance, total_income, total_expense, currency, is_default, description, created_at, user_id) VALUES
('Konto Główne (PLN)', 1500.00, 5000.00, 3500.00, 'PLN', true, 'Główne konto ROR', CURRENT_TIMESTAMP, 1),
('Oszczędności (PLN)', 10000.00, 1000.00, 0.00, 'PLN', false, 'Konto oszczędnościowe', CURRENT_TIMESTAMP, 1),
('Konto Walutowe (EUR)', 500.00, 500.00, 0.00, 'EUR', false, 'Konto na wydatki w EUR', CURRENT_TIMESTAMP, 1);

-- Konta dla Usera 2 (2 konta)
INSERT INTO accounts (name, balance, total_income, total_expense, currency, is_default, description, created_at, user_id) VALUES
('Portfel (PLN)', 3000, 2000.00, 1699.50, 'PLN', true, 'Gotówka', CURRENT_TIMESTAMP, 2),
('Konto USD', 2000.00, 2000.00, 0.00, 'USD', false, NULL, CURRENT_TIMESTAMP, 2);

-- Konta dla Usera 3 (4 konta)
INSERT INTO accounts (name, balance, total_income, total_expense, currency, is_default, description, created_at, user_id) VALUES
('Konto Firmowe (PLN)', 25000.00, 50000.00, 25000.00, 'PLN', true, 'Działalność gospodarcza', CURRENT_TIMESTAMP, 3),
('Konto Prywatne (PLN)', 4000.00, 6000.00, 2000.00, 'PLN', false, NULL, CURRENT_TIMESTAMP, 3),
('Karta Kredytowa (PLN)', -1500.00, 0.00, 1500.00, 'PLN', false, 'Limit 5000 PLN', CURRENT_TIMESTAMP, 3),
('Konto GBP', 800.00, 800.00, 0.00, 'GBP', false, NULL, CURRENT_TIMESTAMP, 3);

-- Konta dla Usera 4 (2 konta)
INSERT INTO accounts (name, balance, total_income, total_expense, currency, is_default, description, created_at, user_id) VALUES
('Konto Główne (EUR)', 3000.00, 4000.00, 1000.00, 'EUR', true, NULL, CURRENT_TIMESTAMP, 4),
('Konto Wakacyjne (EUR)', 1200.00, 1200.00, 0.00, 'EUR', false, 'Na wyjazd', CURRENT_TIMESTAMP, 4);

-- Konta dla Usera 5 (3 konta)
INSERT INTO accounts (name, balance, total_income, total_expense, currency, is_default, description, created_at, user_id) VALUES
('Konto Studenckie (PLN)', 500.00, 1500.00, 1000.00, 'PLN', true, NULL, CURRENT_TIMESTAMP, 5),
('Revolut (PLN)', 150.00, 300.00, 150.00, 'PLN', false, NULL, CURRENT_TIMESTAMP, 5),
('Oszczędności (USD)', 100.00, 100.00, 0.00, 'USD', false, NULL, CURRENT_TIMESTAMP, 5);

-- ===================================================================================
-- KATEGORIE (Kryterium: 3-6 na użytkownika)
-- ===================================================================================

-- Kategorie dla Usera 1 (4 kategorie)
INSERT INTO categories (name, type, icon_path, is_default, user_id) VALUES
('Jedzenie', 'EXPENSE', 'icon/food.png', false, 1),
('Transport', 'EXPENSE', 'icon/transport.png', false, 1),
('Wypłata', 'INCOME', 'icon/income.png', false, 1),
('Rachunki', 'EXPENSE', 'icon/bills.png', false, 1);

-- Kategorie dla Usera 2 (3 kategorie)
INSERT INTO categories (name, type, icon_path, is_default, user_id) VALUES
('Rozrywka', 'EXPENSE', 'icon/entertainment.png', false, 2),
('Mieszkanie', 'EXPENSE', 'icon/home.png', false, 2),
('Premia', 'INCOME', 'icon/bonus.png', false, 2);

-- Kategorie dla Usera 3 (5 kategorii)
INSERT INTO categories (name, type, icon_path, is_default, user_id) VALUES
('Podatki', 'EXPENSE', 'icon/tax.png', false, 3),
('Faktury (Przychód)', 'INCOME', 'icon/invoice.png', false, 3),
('Restauracje', 'EXPENSE', 'icon/restaurant.png', false, 3),
('Samochód', 'EXPENSE', 'icon/car.png', false, 3),
('Inne (Wydatek)', 'EXPENSE', 'icon/other.png', true, 3);

-- Kategorie dla Usera 4 (6 kategorii)
INSERT INTO categories (name, type, icon_path, is_default, user_id) VALUES
('Zakupy (Ubrania)', 'EXPENSE', 'icon/clothes.png', false, 4),
('Prezenty', 'EXPENSE', 'icon/gifts.png', false, 4),
('Zdrowie i Uroda', 'EXPENSE', 'icon/health.png', false, 4),
('Pensja', 'INCOME', 'icon/income.png', false, 4),
('Sport', 'EXPENSE', 'icon/sport.png', false, 4),
('Dzieci', 'EXPENSE', 'icon/children.png', false, 4);

-- Kategorie dla Usera 5 (3 kategorie)
INSERT INTO categories (name, type, icon_path, is_default, user_id) VALUES
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
(120.00, 'Siłownia', 'EXPENSE', NULL, '2024-01-15', NULL, 'MONTH', 1, '2024-11-15', true, CURRENT_TIMESTAMP, 5, 4),
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
(150.00, 'Siłownia', 'EXPENSE', NULL, '2024-01-10', '2025-12-31', 'MONTH', 1, CURRENT_DATE, true, CURRENT_TIMESTAMP, 4, 1);


-- ===================================================================================
-- TRANSAKCJE POWIĄZANE Z SZABLONAMI (20)
-- (Po 2 transakcje na każdy z 10 szablonów)
-- ===================================================================================

-- Transakcje dla Szablonu 1 (Netflix, User 1)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(60.00, 'Netflix', 'EXPENSE', 'Abonament Netflix', '2025-09-05 10:00:00', 4, 1, 1),
(60.00, 'Netflix', 'EXPENSE', 'Abonament Netflix', '2025-10-05 10:00:00', 4, 1, 1);

-- Transakcje dla Szablonu 2 (Czynsz, User 1)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(1500.00, 'Czynsz', 'EXPENSE', 'Opłata za mieszkanie', '2025-09-10 09:00:00', 4, 1, 2),
(1500.00, 'Czynsz', 'EXPENSE', 'Opłata za mieszkanie', '2025-10-10 09:00:00', 4, 1, 2);

-- Transakcje dla Szablonu 3 (Siłownia, User 2)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(120.00, 'Siłownia', 'EXPENSE', NULL, '2024-09-15 18:00:00', 5, 4, 3),
(120.00, 'Siłownia', 'EXPENSE', NULL, '2024-10-15 18:00:00', 5, 4, 3);

-- Transakcje dla Szablonu 4 (Telefon, User 2)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(50.00, 'Telefon', 'EXPENSE', 'Abonament komórkowy', '2024-09-01 12:00:00', 6, 4, 4),
(50.00, 'Telefon', 'EXPENSE', 'Abonament komórkowy', '2024-10-01 12:00:00', 6, 4, 4);

-- Transakcje dla Szablonu 5 (ZUS, User 3)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(1800.00, 'ZUS', 'EXPENSE', 'Składka ZUS', '2024-09-20 08:00:00', 8, 6, 5),
(1800.00, 'ZUS', 'EXPENSE', 'Składka ZUS', '2024-10-20 08:00:00', 8, 6, 5);

-- Transakcje dla Szablonu 6 (Leasing, User 3)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(1200.00, 'Leasing', 'EXPENSE', 'Rata leasingowa za auto', '2024-09-15 11:00:00', 11, 6, 6),
(1200.00, 'Leasing', 'EXPENSE', 'Rata leasingowa za auto', '2024-10-15 11:00:00', 11, 6, 6);

-- Transakcje dla Szablonu 7 (Oszczędności, User 4)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(200.00, 'Oszczędności na wakacje', 'EXPENSE', 'Przelew na konto wakacyjne', '2024-09-01 10:00:00', 18, 10, 7),
(200.00, 'Oszczędności na wakacje', 'EXPENSE', 'Przelew na konto wakacyjne', '2024-10-01 10:00:00', 18, 10, 7);

-- Transakcje dla Szablonu 8 (Ubezpieczenie, User 4 - szablon nieaktywny, ale transakcje mogły być)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(50.00, 'Ubezpieczenie zdrowotne', 'EXPENSE', NULL, '2024-08-10 13:00:00', 15, 10, 8),
(50.00, 'Ubezpieczenie zdrowotne', 'EXPENSE', NULL, '2024-09-10 13:00:00', 15, 10, 8);

-- Transakcje dla Szablonu 9 (Stypendium, User 5)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(800.00, 'Stypendium naukowe', 'INCOME', 'Wpływ z uczelni', '2024-10-15 14:00:00', 20, 12, 9),
(800.00, 'Stypendium naukowe', 'INCOME', 'Wpływ z uczelni', '2024-11-15 14:00:00', 20, 12, 9);

-- Transakcje dla Szablonu 10 (Czesne, User 5)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(500.00, 'Czesne za studia', 'EXPENSE', 'Opłata semestralna', '2024-10-01 16:00:00', 19, 12, 10),
(500.00, 'Czesne za studia', 'EXPENSE', 'Opłata semestralna', '2024-11-01 16:00:00', 19, 12, 10);

-- ===================================================================================
-- TRANSAKCJE ZWYKŁE (NIEPOWIĄZANE Z SZABLONAMI) (40)
-- (ID zaczynają się od 21, recurring_transaction_id = NULL)
-- ===================================================================================

-- Transakcje dla Usera 1 (8 transakcji)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(120.50, 'Zakupy spożywcze', 'EXPENSE', 'Biedronka', '2025-10-20 18:30:00', 1, 1, NULL),
(50.00, 'Bilet miesięczny', 'EXPENSE', NULL, '2025-10-01 08:00:00', 2, 1, NULL),
(5000.00, 'Wypłata', 'INCOME', 'Wynagrodzenie za Październik', '2025-10-28 10:00:00', 3, 1, NULL),
(200.00, 'Paliwo', 'EXPENSE', 'Orlen', '2025-10-15 17:00:00', 2, 1, NULL),
(70.00, 'Kino', 'EXPENSE', 'Cinema City', '2025-10-12 20:00:00', 1, 1, NULL), -- Użycie kat. Jedzenie zamiast Rozrywka (test)
(1000.00, 'Przelew na oszczędności', 'EXPENSE', NULL, '2025-10-28 11:00:00', 4, 1, NULL),
(30.00, 'Restauracja (EUR)', 'EXPENSE', 'Obiad w Berlinie', '2025-10-05 14:00:00', 1, 3, NULL),
(1000.00, 'Wpłata na oszczędności', 'INCOME', NULL, '2025-10-28 11:01:00', 3, 2, NULL); -- Symulacja transferu

-- Transakcje dla Usera 2 (8 transakcji)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(300.00, 'Bilet na koncert', 'EXPENSE', 'Coldplay', '2024-10-02 15:00:00', 5, 4, NULL),
(200.00, 'Prąd', 'EXPENSE', 'Rachunek za prąd', '2024-10-18 10:00:00', 6, 4, NULL),
(80.00, 'Gaz', 'EXPENSE', 'Rachunek za gaz', '2024-10-18 10:05:00', 6, 4, NULL),
(1000.00, 'Premia', 'INCOME', 'Premia kwartalna', '2024-10-10 13:00:00', 7, 4, NULL),
(45.00, 'Pyszne.pl', 'EXPENSE', 'Pizza', '2024-10-22 19:00:00', 5, 4, NULL),
(500.00, 'Wpłata na konto USD', 'INCOME', 'Przewalutowanie', '2024-10-05 12:00:00', 7, 5, NULL),
(25.50, 'Uber', 'EXPENSE', NULL, '2024-10-12 23:00:00', 5, 4, NULL),
(60.00, 'Internet', 'EXPENSE', 'Opłata za internet', '2024-10-03 11:00:00', 6, 4, NULL);

-- Transakcje dla Usera 3 (8 transakcji)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(250.00, 'Obiad biznesowy', 'EXPENSE', 'Restauracja Sowa', '2024-10-21 14:00:00', 10, 6, NULL),
(15000.00, 'Faktura 10/2024', 'INCOME', 'Klient X', '2024-10-10 09:00:00', 9, 6, NULL),
(10000.00, 'Faktura 11/2024', 'INCOME', 'Klient Y', '2024-10-15 10:00:00', 9, 6, NULL),
(150.00, 'Myjnia', 'EXPENSE', 'Myjnia ręczna', '2024-10-20 12:00:00', 11, 7, NULL),
(1500.00, 'Spłata karty kredytowej', 'EXPENSE', NULL, '2024-10-25 10:00:00', 12, 7, NULL),
(300.00, 'Zakupy na Allegro', 'EXPENSE', 'Części do komputera', '2024-10-19 19:00:00', 12, 8, NULL),
(150.00, 'Książki', 'EXPENSE', 'Księgarnia techniczna', '2024-10-05 13:00:00', 12, 7, NULL),
(100.00, 'Hotel (GBP)', 'EXPENSE', 'Pobyt w Londynie', '2024-10-11 15:00:00', 10, 9, NULL);

-- Transakcje dla Usera 4 (8 transakcji)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(4000.00, 'Pensja (EUR)', 'INCOME', 'Wynagrodzenie 10/2024', '2024-10-28 09:00:00', 16, 10, NULL),
(150.00, 'Zara', 'EXPENSE', 'Nowa kurtka', '2024-10-19 16:00:00', 13, 10, NULL),
(50.00, 'Wizyta u lekarza (EUR)', 'EXPENSE', 'Internista', '2024-10-14 11:00:00', 15, 10, NULL),
(40.00, 'Prezent urodzinowy', 'EXPENSE', 'Dla Anny', '2024-10-08 17:00:00', 14, 10, NULL),
(80.00, 'Zabawki', 'EXPENSE', 'Lego', '2024-10-22 18:00:00', 18, 10, NULL),
(60.00, 'Bilet do muzeum', 'EXPENSE', NULL, '2024-10-26 13:00:00', 17, 10, NULL),
(1000.00, 'Przelew na konto wakacyjne', 'INCOME', 'Zasilenie', '2024-10-28 10:00:00', 16, 11, NULL),
(25.00, 'Apteka (EUR)', 'EXPENSE', 'Leki', '2024-10-14 12:00:00', 15, 10, NULL);

-- Transakcje dla Usera 5 (8 transakcji)
INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(15.00, 'Kawa', 'EXPENSE', 'Starbucks', '2024-10-28 09:15:00', 21, 12, NULL),
(30.00, 'Obiad na stołówce', 'EXPENSE', NULL, '2024-10-28 14:00:00', 21, 12, NULL),
(300.00, 'Praca dorywcza', 'INCOME', 'Zlecenie graficzne', '2024-10-15 19:00:00', 20, 13, NULL),
(45.00, 'Bilet na pociąg', 'EXPENSE', 'PKP Intercity', '2024-10-25 16:00:00', 21, 12, NULL),
(150.00, 'Książki na studia', 'EXPENSE', 'Ksero', '2024-10-07 11:00:00', 19, 12, NULL),
(20.00, 'Amazon (USD)', 'EXPENSE', 'Ebook', '2024-10-11 20:00:00', 19, 14, NULL),
(100.00, 'Wypłata z bankomatu', 'EXPENSE', 'Prowizja', '2024-10-20 10:00:00', 21, 13, NULL),
(25.00, 'Kebab', 'EXPENSE', NULL, '2024-10-26 22:00:00', 21, 12, NULL);