DELETE FROM transactions;
DELETE FROM recurring_transactions;
DELETE FROM categories;
DELETE FROM accounts;
DELETE FROM user_sessions;
DELETE FROM users;

ALTER TABLE users ALTER COLUMN id RESTART WITH 1;
ALTER TABLE accounts ALTER COLUMN id RESTART WITH 1;
ALTER TABLE categories ALTER COLUMN id RESTART WITH 1;
ALTER TABLE recurring_transactions ALTER COLUMN id RESTART WITH 1;
ALTER TABLE transactions ALTER COLUMN id RESTART WITH 1;

INSERT INTO users (name, surname, email, password, status, created_at, mfa_enabled, two_factor_secret, temp_two_factor_secret, request_close_at) VALUES
('Jan', 'Kowalski', 'jan.kowalski@example.com', '{noop}password123', 'ACTIVE', CURRENT_TIMESTAMP, false, NULL, NULL, NULL);

INSERT INTO accounts (name, balance, total_income, total_expense, account_status, account_type, currency, is_default, description, budget_type, created_at, include_in_total_balance, user_id) VALUES
('Main account', 1500.00, 5000.00, 3500.00, 'ACTIVE', 'PERSONAL', 'PLN', true, null, 'NONE', CURRENT_TIMESTAMP, true, 1),
('Saving account', 10000.00, 1000.00, 0.00, 'ACTIVE', 'PERSONAL', 'PLN', false, null, 'MONTHLY', CURRENT_TIMESTAMP, true, 1),
('Personal Account', 10000.00, 1000.00, 0.00, 'ACTIVE', 'PERSONAL', 'PLN', false, null, 'MONTHLY', CURRENT_TIMESTAMP, true, 1);

INSERT INTO categories (name, type, icon_key, is_default, user_id) VALUES
('Salary', 'INCOME', 'categories/loan-icon.png', false, 1),
('Bonus', 'INCOME', 'categories/gift-hand-present-icon.png', false, 1), -- Dodałem .png
('Investment Profit', 'INCOME', 'categories/investment-analysis-icon.png', false, 1),
('Gift Received', 'INCOME', 'categories/present-icon.png', false, 1);

INSERT INTO categories (name, type, icon_key, is_default, user_id) VALUES
('Food & Groceries', 'EXPENSE', 'categories/burger-icon.png', false, 1),
('Transport', 'EXPENSE', 'categories/car-icon.png', false, 1),
('Housing & Rent', 'EXPENSE', 'categories/house-icon.png', false, 1),
('Utilities & Bills', 'EXPENSE', 'categories/payday-icon.png', false, 1),
('Health & Medicine', 'EXPENSE', 'categories/heart-icon.png', false, 1),
('Education', 'EXPENSE', 'categories/graduation-cap-icon.png', false, 1),
('Entertainment', 'EXPENSE', 'categories/concert-icon.png', false, 1), -- Dodałem .png
('Clothing & Accessories', 'EXPENSE', 'categories/shirt-icon.png', false, 1),
('Subscriptions', 'EXPENSE', 'categories/four-squares-line-icon.png', false, 1),
('Insurance', 'EXPENSE', 'categories/shield-checkmark-black-icon.png', false, 1);

INSERT INTO categories (name, type, icon_key, is_default, user_id) VALUES
('Savings', 'GENERAL', 'categories/piggy-saving-icon.png', false, 1),
('Investments', 'GENERAL', 'categories/money-bag-icon.png', false, 1),
('Debt & Loans', 'GENERAL', 'categories/house-hand-mortgage-icon.png', false, 1),
('Uncategorized', 'GENERAL', 'categories/four-squares-line-icon.png', false, 1),
('Other', 'GENERAL', 'categories/question-mark-icon.png', true, 1);

INSERT INTO recurring_transactions (amount, title, type, description, start_date, end_date, recurring_interval, recurring_value, next_occurrence, is_active, created_at, category_id, account_id) VALUES
(5000.00, 'Wypłata wynagrodzenia', 'INCOME', 'Comiesięczny wpływ od pracodawcy', (CURRENT_DATE - INTERVAL '3 months'), NULL, 'MONTH', 1, (CURRENT_DATE + INTERVAL '20 days'), true, CURRENT_TIMESTAMP, 1, 1),
(2500.00, 'Czynsz za mieszkanie', 'EXPENSE', 'Opłata za wynajem + czynsz administracyjny', (CURRENT_DATE - INTERVAL '3 months'), NULL, 'MONTH', 1, (CURRENT_DATE + INTERVAL '5 days'), true, CURRENT_TIMESTAMP, 7, 1),
(60.00, 'Netflix', 'EXPENSE', 'Plan Premium 4K', (CURRENT_DATE - INTERVAL '3 months'), NULL, 'MONTH', 1, (CURRENT_DATE + INTERVAL '2 days'), true, CURRENT_TIMESTAMP, 13, 1),
(120.00, 'CityFit', 'EXPENSE', 'Karnet miesięczny open', (CURRENT_DATE - INTERVAL '3 months'), NULL, 'MONTH', 1, (CURRENT_DATE + INTERVAL '10 days'), true, CURRENT_TIMESTAMP, 9, 3),
(79.99, 'UPC Internet', 'EXPENSE', 'Faktura za internet 600mb/s', (CURRENT_DATE - INTERVAL '3 months'), NULL, 'MONTH', 1, (CURRENT_DATE + INTERVAL '15 days'), true, CURRENT_TIMESTAMP, 8, 1);

INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(5000.00, 'Wypłata wynagrodzenia', 'INCOME', 'Wypłata styczeń/luty', CURRENT_TIMESTAMP - INTERVAL '2 months', 1, 1, 1),
(5000.00, 'Wypłata wynagrodzenia', 'INCOME', 'Wypłata luty/marzec', CURRENT_TIMESTAMP - INTERVAL '1 month', 1, 1, 1),
(5000.00, 'Wypłata wynagrodzenia', 'INCOME', 'Wypłata bieżąca', CURRENT_TIMESTAMP - INTERVAL '2 days', 1, 1, 1),

(2500.00, 'Czynsz za mieszkanie', 'EXPENSE', 'Czynsz za poprzedni miesiąc', CURRENT_TIMESTAMP - INTERVAL '65 days', 7, 1, 2),
(2500.00, 'Czynsz za mieszkanie', 'EXPENSE', 'Czynsz za zeszły miesiąc', CURRENT_TIMESTAMP - INTERVAL '35 days', 7, 1, 2),
(2500.00, 'Czynsz za mieszkanie', 'EXPENSE', 'Czynsz bieżący', CURRENT_TIMESTAMP - INTERVAL '5 days', 7, 1, 2),

(60.00, 'Netflix', 'EXPENSE', 'Abonament', CURRENT_TIMESTAMP - INTERVAL '62 days', 13, 1, 3),
(60.00, 'Netflix', 'EXPENSE', 'Abonament', CURRENT_TIMESTAMP - INTERVAL '32 days', 13, 1, 3),

(120.00, 'CityFit', 'EXPENSE', 'Karnet', CURRENT_TIMESTAMP - INTERVAL '70 days', 9, 3, 4),
(120.00, 'CityFit', 'EXPENSE', 'Karnet', CURRENT_TIMESTAMP - INTERVAL '40 days', 9, 3, 4),
(120.00, 'CityFit', 'EXPENSE', 'Karnet', CURRENT_TIMESTAMP - INTERVAL '10 days', 9, 3, 4),

(79.99, 'UPC Internet', 'EXPENSE', 'Faktura', CURRENT_TIMESTAMP - INTERVAL '45 days', 8, 1, 5),
(79.99, 'UPC Internet', 'EXPENSE', 'Faktura', CURRENT_TIMESTAMP - INTERVAL '15 days', 8, 1, 5);

INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(152.40, 'Zakupy Biedronka', 'EXPENSE', 'Zakupy tygodniowe', CURRENT_TIMESTAMP - INTERVAL '88 days', 5, 1, NULL),
(45.90, 'Żabka', 'EXPENSE', 'Szybkie zakupy', CURRENT_TIMESTAMP - INTERVAL '85 days', 5, 1, NULL),
(210.00, 'Lidl', 'EXPENSE', 'Duże zakupy domowe', CURRENT_TIMESTAMP - INTERVAL '80 days', 5, 1, NULL),
(18.50, 'Piekarnia Putka', 'EXPENSE', 'Chleb i bułki', CURRENT_TIMESTAMP - INTERVAL '78 days', 5, 1, NULL),
(320.15, 'Auchan', 'EXPENSE', 'Zapasy na miesiąc', CURRENT_TIMESTAMP - INTERVAL '75 days', 5, 1, NULL),
(12.99, 'Warzywniak', 'EXPENSE', 'Świeże owoce', CURRENT_TIMESTAMP - INTERVAL '74 days', 5, 1, NULL),
(89.50, 'Biedronka', 'EXPENSE', 'Uzupełnienie lodówki', CURRENT_TIMESTAMP - INTERVAL '70 days', 5, 1, NULL),
(55.00, 'Lunch w pracy', 'EXPENSE', 'Kantyna', CURRENT_TIMESTAMP - INTERVAL '69 days', 5, 1, NULL),
(42.00, 'Pizza Hut', 'EXPENSE', 'Wyjście ze znajomymi', CURRENT_TIMESTAMP - INTERVAL '68 days', 5, 1, NULL),
(15.00, 'Kawa Costa', 'EXPENSE', 'Kawa na mieście', CURRENT_TIMESTAMP - INTERVAL '67 days', 5, 1, NULL),
(180.00, 'Lidl', 'EXPENSE', 'Zakupy spożywcze', CURRENT_TIMESTAMP - INTERVAL '65 days', 5, 1, NULL),
(25.00, 'Żabka', 'EXPENSE', 'Napoje i przekąski', CURRENT_TIMESTAMP - INTERVAL '64 days', 5, 1, NULL),
(99.90, 'Carrefour', 'EXPENSE', 'Chemia gospodarcza', CURRENT_TIMESTAMP - INTERVAL '62 days', 5, 1, NULL),
(200.00, 'Biedronka', 'EXPENSE', 'Zakupy weekendowe', CURRENT_TIMESTAMP - INTERVAL '60 days', 5, 1, NULL),
(35.00, 'McDonalds', 'EXPENSE', 'Szybki obiad', CURRENT_TIMESTAMP - INTERVAL '59 days', 5, 1, NULL),
(14.50, 'Lody', 'EXPENSE', 'Deser na spacerze', CURRENT_TIMESTAMP - INTERVAL '58 days', 5, 1, NULL),
(165.20, 'Kaufland', 'EXPENSE', 'Zakupy spożywcze', CURRENT_TIMESTAMP - INTERVAL '55 days', 5, 1, NULL),
(29.99, 'Rossmann', 'EXPENSE', 'Kosmetyki', CURRENT_TIMESTAMP - INTERVAL '53 days', 5, 1, NULL),
(215.50, 'Lidl', 'EXPENSE', 'Zakupy', CURRENT_TIMESTAMP - INTERVAL '50 days', 5, 1, NULL),
(45.00, 'Kebab King', 'EXPENSE', 'Kolacja', CURRENT_TIMESTAMP - INTERVAL '49 days', 5, 1, NULL),
(150.00, 'Paliwo Orlen', 'EXPENSE', 'Tankowanie do pełna', CURRENT_TIMESTAMP - INTERVAL '82 days', 6, 1, NULL),
(25.00, 'Uber', 'EXPENSE', 'Powrót z imprezy', CURRENT_TIMESTAMP - INTERVAL '79 days', 6, 1, NULL),
(4.40, 'Bilet ZTM', 'EXPENSE', 'Dojazd do pracy', CURRENT_TIMESTAMP - INTERVAL '76 days', 6, 1, NULL),
(35.00, 'Bolt', 'EXPENSE', 'Przejazd na spotkanie', CURRENT_TIMESTAMP - INTERVAL '72 days', 6, 1, NULL),
(200.00, 'Paliwo BP', 'EXPENSE', 'Tankowanie', CURRENT_TIMESTAMP - INTERVAL '63 days', 6, 1, NULL),
(15.00, 'Parking', 'EXPENSE', 'Centrum handlowe', CURRENT_TIMESTAMP - INTERVAL '57 days', 6, 1, NULL),
(45.00, 'Uber', 'EXPENSE', 'Przejazd na dworzec', CURRENT_TIMESTAMP - INTERVAL '51 days', 6, 1, NULL),
(180.00, 'Paliwo CircleK', 'EXPENSE', 'Tankowanie', CURRENT_TIMESTAMP - INTERVAL '42 days', 6, 1, NULL),
(110.00, 'Bilet PKP', 'EXPENSE', 'Wyjazd do Krakowa', CURRENT_TIMESTAMP - INTERVAL '30 days', 6, 1, NULL),
(28.00, 'Taxi', 'EXPENSE', 'Powrót z dworca', CURRENT_TIMESTAMP - INTERVAL '28 days', 6, 1, NULL),
(150.00, 'Apteka', 'EXPENSE', 'Leki na przeziębienie', CURRENT_TIMESTAMP - INTERVAL '48 days', 9, 1, NULL),
(250.00, 'Stomatolog', 'EXPENSE', 'Przegląd i higienizacja', CURRENT_TIMESTAMP - INTERVAL '35 days', 9, 1, NULL),
(45.00, 'Empik', 'EXPENSE', 'Książka', CURRENT_TIMESTAMP - INTERVAL '22 days', 19, 1, NULL),
(89.00, 'Prezent urodzinowy', 'EXPENSE', 'Dla mamy', CURRENT_TIMESTAMP - INTERVAL '12 days', 19, 1, NULL),
(50.00, 'Kino Helios', 'EXPENSE', 'Bilety na Diunę', CURRENT_TIMESTAMP - INTERVAL '44 days', 11, 1, NULL),
(35.00, 'Popcorn i Cola', 'EXPENSE', 'Przekąski w kinie', CURRENT_TIMESTAMP - INTERVAL '44 days', 11, 1, NULL),
(120.00, 'Kręgle', 'EXPENSE', 'Wyjście integracyjne', CURRENT_TIMESTAMP - INTERVAL '25 days', 11, 1, NULL),
(200.00, 'Koncert', 'EXPENSE', 'Bilety na koncert', CURRENT_TIMESTAMP - INTERVAL '15 days', 11, 1, NULL),
(130.00, 'Biedronka', 'EXPENSE', 'Zakupy', CURRENT_TIMESTAMP - INTERVAL '29 days', 5, 1, NULL),
(22.00, 'Żabka', 'EXPENSE', 'Drobne zakupy', CURRENT_TIMESTAMP - INTERVAL '27 days', 5, 1, NULL),
(55.00, 'Restauracja Thai', 'EXPENSE', 'Obiad niedzielny', CURRENT_TIMESTAMP - INTERVAL '26 days', 5, 1, NULL),
(190.00, 'Lidl', 'EXPENSE', 'Zapasy', CURRENT_TIMESTAMP - INTERVAL '20 days', 5, 1, NULL),
(40.00, 'Kawiarnia', 'EXPENSE', 'Spotkanie', CURRENT_TIMESTAMP - INTERVAL '18 days', 5, 1, NULL),
(300.00, 'Auchan', 'EXPENSE', 'Duże zakupy', CURRENT_TIMESTAMP - INTERVAL '14 days', 5, 1, NULL),
(60.00, 'Burger King', 'EXPENSE', 'Kolacja', CURRENT_TIMESTAMP - INTERVAL '13 days', 5, 1, NULL),
(85.00, 'Delikatesy', 'EXPENSE', 'Dobre wino i ser', CURRENT_TIMESTAMP - INTERVAL '11 days', 5, 1, NULL),
(140.00, 'Biedronka', 'EXPENSE', 'Zakupy', CURRENT_TIMESTAMP - INTERVAL '9 days', 5, 1, NULL),
(18.00, 'Piekarnia', 'EXPENSE', 'Śniadanie', CURRENT_TIMESTAMP - INTERVAL '8 days', 5, 1, NULL),
(210.00, 'Lidl', 'EXPENSE', 'Zakupy', CURRENT_TIMESTAMP - INTERVAL '6 days', 5, 1, NULL),
(35.00, 'Bar Mleczny', 'EXPENSE', 'Obiad', CURRENT_TIMESTAMP - INTERVAL '4 days', 5, 1, NULL),
(12.50, 'Sklep osiedlowy', 'EXPENSE', 'Woda i mleko', CURRENT_TIMESTAMP - INTERVAL '3 days', 5, 1, NULL),
(250.00, 'Supermarket', 'EXPENSE', 'Zakupy na imprezę', CURRENT_TIMESTAMP - INTERVAL '2 days', 5, 1, NULL),
(45.00, 'Alkohol', 'EXPENSE', 'Wino do kolacji', CURRENT_TIMESTAMP - INTERVAL '2 days', 5, 1, NULL),
(30.00, 'Taxi', 'EXPENSE', 'Powrót do domu', CURRENT_TIMESTAMP - INTERVAL '2 days', 6, 1, NULL),
(500.00, 'Premia kwartalna', 'INCOME', 'Dodatek za wyniki', CURRENT_TIMESTAMP - INTERVAL '40 days', 2, 1, NULL),
(200.00, 'Zwrot od znajomego', 'INCOME', 'Za pizzę', CURRENT_TIMESTAMP - INTERVAL '10 days', 19, 1, NULL),
(1000.00, 'Prezent od babci', 'INCOME', 'Urodziny', CURRENT_TIMESTAMP - INTERVAL '12 days', 4, 1, NULL),
(50.00, 'Sprzedaż OLX', 'INCOME', 'Stare gry', CURRENT_TIMESTAMP - INTERVAL '5 days', 19, 1, NULL),
(15.00, 'Odsetki bankowe', 'INCOME', 'Kapitalizacja', CURRENT_TIMESTAMP - INTERVAL '1 day', 3, 1, NULL),
(80.00, 'Fryzjer', 'EXPENSE', 'Strzyżenie', CURRENT_TIMESTAMP - INTERVAL '33 days', 9, 1, NULL),
(120.00, 'Hebe', 'EXPENSE', 'Prezenty', CURRENT_TIMESTAMP - INTERVAL '14 days', 12, 1, NULL),
(50.00, 'Myjnia', 'EXPENSE', 'Mycie auta', CURRENT_TIMESTAMP - INTERVAL '7 days', 6, 1, NULL),
(30.00, 'Kino', 'EXPENSE', 'Wtorek w kinie', CURRENT_TIMESTAMP - INTERVAL '3 days', 11, 1, NULL),
(99.00, 'Spotify Yearly', 'EXPENSE', 'Dopłata do rocznego', CURRENT_TIMESTAMP - INTERVAL '60 days', 13, 1, NULL),
(25.00, 'Kebab', 'EXPENSE', 'Nocne jedzenie', CURRENT_TIMESTAMP - INTERVAL '1 day', 5, 1, NULL);

INSERT INTO transactions (amount, title, type, description, transaction_date, category_id, account_id, recurring_transaction_id) VALUES
(2000.00, 'Przelew na oszczędności', 'INCOME', 'Zasilenie konta', CURRENT_TIMESTAMP - INTERVAL '80 days', 15, 2, NULL),
(100.00, 'Wypłata z bankomatu', 'EXPENSE', 'Potrzebna gotówka', CURRENT_TIMESTAMP - INTERVAL '60 days', 19, 2, NULL),
(500.00, 'Lokata krótkoterminowa', 'EXPENSE', 'Założenie lokaty', CURRENT_TIMESTAMP - INTERVAL '40 days', 16, 2, NULL),
(1500.00, 'Przelew na oszczędności', 'INCOME', 'Zasilenie konta', CURRENT_TIMESTAMP - INTERVAL '20 days', 15, 2, NULL),
(300.00, 'Awaria hydraulika', 'EXPENSE', 'Nagły wydatek z oszczędności', CURRENT_TIMESTAMP - INTERVAL '15 days', 7, 2, NULL),
(299.00, 'Zalando', 'EXPENSE', 'Nowe buty', CURRENT_TIMESTAMP - INTERVAL '85 days', 12, 3, NULL),
(150.00, 'H&M', 'EXPENSE', 'Koszule do pracy', CURRENT_TIMESTAMP - INTERVAL '82 days', 12, 3, NULL),
(450.00, 'Kurtka Zimowa', 'EXPENSE', 'Sklep górski', CURRENT_TIMESTAMP - INTERVAL '70 days', 12, 3, NULL),
(89.99, 'Decathlon', 'EXPENSE', 'Sprzęt sportowy', CURRENT_TIMESTAMP - INTERVAL '68 days', 11, 3, NULL),
(120.00, 'Nike', 'EXPENSE', 'Spodenki na siłownie', CURRENT_TIMESTAMP - INTERVAL '65 days', 12, 3, NULL),
(600.00, 'Serwis Rowerowy', 'EXPENSE', 'Przegląd generalny', CURRENT_TIMESTAMP - INTERVAL '55 days', 11, 3, NULL),
(150.00, 'Części rowerowe', 'EXPENSE', 'Nowy łańcuch', CURRENT_TIMESTAMP - INTERVAL '54 days', 11, 3, NULL),
(200.00, 'Vistula', 'EXPENSE', 'Krawat i spinki', CURRENT_TIMESTAMP - INTERVAL '45 days', 12, 3, NULL),
(300.00, 'Gry PlayStation', 'EXPENSE', 'Promocja PS Store', CURRENT_TIMESTAMP - INTERVAL '40 days', 11, 3, NULL),
(80.00, 'Steam', 'EXPENSE', 'Gra indie', CURRENT_TIMESTAMP - INTERVAL '38 days', 11, 3, NULL),
(1200.00, 'Nowy Monitor', 'EXPENSE', 'Elektronika', CURRENT_TIMESTAMP - INTERVAL '30 days', 19, 3, NULL),
(45.00, 'Myszka komputerowa', 'EXPENSE', 'Akcesoria', CURRENT_TIMESTAMP - INTERVAL '28 days', 19, 3, NULL),
(80.00, 'Reserved', 'EXPENSE', 'T-shirty', CURRENT_TIMESTAMP - INTERVAL '25 days', 12, 3, NULL),
(220.00, 'CCC', 'EXPENSE', 'Buty eleganckie', CURRENT_TIMESTAMP - INTERVAL '22 days', 12, 3, NULL),
(60.00, 'Pasek skórzany', 'EXPENSE', 'Dodatki', CURRENT_TIMESTAMP - INTERVAL '20 days', 12, 3, NULL),
(150.00, 'Fryzjer Barber', 'EXPENSE', 'Usługa premium', CURRENT_TIMESTAMP - INTERVAL '18 days', 9, 3, NULL),
(350.00, 'Okulary przeciwsłoneczne', 'EXPENSE', 'RayBan', CURRENT_TIMESTAMP - INTERVAL '15 days', 12, 3, NULL),
(90.00, 'Kosmetyki', 'EXPENSE', 'Perfumy', CURRENT_TIMESTAMP - INTERVAL '12 days', 9, 3, NULL),
(40.00, 'Gadżety', 'EXPENSE', 'AliExpress', CURRENT_TIMESTAMP - INTERVAL '10 days', 19, 3, NULL),
(500.00, 'Prezent ślubny', 'EXPENSE', 'Koperta dla znajomych', CURRENT_TIMESTAMP - INTERVAL '8 days', 19, 3, NULL),
(100.00, 'Kwiaty', 'EXPENSE', 'Dla żony', CURRENT_TIMESTAMP - INTERVAL '8 days', 19, 3, NULL),
(250.00, 'Badania krwi', 'EXPENSE', 'Diagnostyka prywatna', CURRENT_TIMESTAMP - INTERVAL '5 days', 9, 3, NULL),
(120.00, 'Suplementy', 'EXPENSE', 'Witaminy', CURRENT_TIMESTAMP - INTERVAL '4 days', 9, 3, NULL),
(65.00, 'Empik', 'EXPENSE', 'Płyta winylowa', CURRENT_TIMESTAMP - INTERVAL '2 days', 11, 3, NULL),
(110.00, 'Restauracja Sushi', 'EXPENSE', 'Randka', CURRENT_TIMESTAMP - INTERVAL '1 day', 5, 3, NULL);



