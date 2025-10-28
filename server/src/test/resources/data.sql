INSERT INTO accounts (name, balance, total_income, total_expense, currency, is_default, description, created_at)
VALUES
    ('Main Account', 0.00, 0.00, 0.00, 'USD', false, 'Primary checking account', NOW()),
    ('Savings Account', 0.00, 0.00, 0.00, 'EUR', false, 'Long term savings', NOW()),
    ('Backup Fund', 0.00, 0.00, 0.00, 'PLN', false, 'Emergency backup funds', NOW()),
    ('Investment Account', 0.00, 0.00, 0.00, 'GBP', false, 'Funds for investments', NOW()),
    ('Crypto Wallet', 0.00, 0.00, 0.00, 'GBP', false, 'Bitcoin wallet for hodling', NOW());

INSERT INTO categories (name, type, icon_path, is_default)
VALUES
    ('Food & Drink', 'EXPENSE', '/icons/food.png', false),
    ('Salary', 'INCOME', '/icons/salary.png', true),
    ('Travel', 'EXPENSE', '/icons/travel.png', false),
    ('Investment', 'INCOME', '/icons/investment.png', false),
    ('Health', 'EXPENSE', '/icons/health.png', false);

INSERT INTO recurring_transactions (
    amount, title, type, description,
    start_date, end_date,
    recurring_interval, recurring_value,
    next_occurrence, is_active,
    created_at, category_id, account_id
) VALUES
    (100.00, 'Monthly Subscription', 'EXPENSE', 'Spotify subscription monthly',
     '2025-09-01', NULL,
     'MONTH', 1,
     '2025-10-01', TRUE,
     '2025-09-01 00:00:00', 1, 1),
    (50.00, 'Gym Membership', 'EXPENSE', 'Gym fee every 3 months',
     '2025-07-01', NULL,
     'MONTH', 3,
     '2025-10-01', TRUE,
     '2025-07-01 00:00:00', 2, 1),
    (2000.00, 'Salary', 'INCOME', 'Monthly salary from employer',
     '2025-01-01', NULL,
     'MONTH', 1,
     '2025-10-01', TRUE,
     '2025-01-01 00:00:00', 3, 2),
    (20.00, 'Weekly allowance', 'INCOME', 'Allowance every week',
     '2025-09-20', '2025-12-31',
     'MONTH', 1,
     '2025-09-27', TRUE,
     '2025-09-20 00:00:00', 4, 1);

INSERT INTO transactions (amount, title, type, description, transaction_date, photo_path, category_id, account_id, recurring_transaction_id)
VALUES
    (100.00, 'Zakup książki', 'EXPENSE', 'Zakup książki "Java dla początkujących"', '2025-09-22 10:00:00', '/images/ksiazka.jpg', 1, 1, NULL),
    (2000.00, 'Wypłata za wrzesień', 'INCOME', 'Wynagrodzenie za wrzesień 2025', '2025-09-30 09:00:00', NULL, 2, 1, 1),
    (50.00, 'Obiad w restauracji', 'EXPENSE', 'Obiad w restauracji "Kuchnia Polska"', '2025-09-22 13:00:00', '/images/obiad.jpg', 3, 1, 1),
    (150.00, 'Zakup biletu na koncert', 'EXPENSE', 'Bilet na koncert zespołu XYZ', '2025-09-23 18:00:00', '/images/bilet.jpg', 1, 2, 2),
    (500.00, 'Wypłata za wrzesień', 'INCOME', 'Wynagrodzenie za wrzesień 2025', '2025-09-30 09:00:00', NULL, 2, 2, NULL),
    (75.50, 'Zakupy spożywcze', 'EXPENSE', 'Zakupy w supermarkecie', '2025-09-21 17:30:00', '/images/zakupy.jpg', 3, 2, NULL),
    (20.00, 'Kawa i ciastko', 'EXPENSE', 'Popołudniowa kawa', '2025-09-20 15:45:00', NULL, 1, 3, NULL),
    (120.00, 'Rachunek za prąd', 'EXPENSE', 'Opłata miesięczna za prąd', '2025-09-15 12:00:00', NULL, 2, 1, NULL),
    (300.00, 'Naprawa samochodu', 'EXPENSE', 'Wymiana oleju i filtrów', '2025-09-10 09:30:00', '/images/auto.jpg', 3, 1, NULL),
    (250.00, 'Zakup ubrania', 'EXPENSE', 'Nowa kurtka', '2025-09-05 14:20:00', '/images/ubranie.jpg', 1, 2, NULL),
    (40.00, 'Bilet autobusowy', 'EXPENSE', 'Dojazd do pracy', '2025-09-18 08:00:00', NULL, 2, 3, NULL),
    (15.00, 'Subskrypcja streaming', 'EXPENSE', 'Netflix', '2025-09-01 20:00:00', NULL, 3, 3, NULL),
    (100.00, 'Sprzedaż używanych książek', 'INCOME', 'Sprzedaż na lokalnym rynku', '2025-09-12 11:00:00', NULL, 1, 1, 1),
    (500.00, 'Premia kwartalna', 'INCOME', 'Premia od pracodawcy', '2025-09-01 09:00:00', NULL, 2, 1, NULL),
    (80.00, 'Koszt internetu', 'EXPENSE', 'Opłata miesięczna internet', '2025-09-03 10:00:00', NULL, 3, 2, NULL),
    (60.00, 'Lunch służbowy', 'EXPENSE', 'Spotkanie z klientem', '2025-09-16 13:00:00', '/images/lunch.jpg', 1, 3, NULL),
    (30.00, 'Prezent urodzinowy', 'EXPENSE', 'Upominek dla kolegi', '2025-09-20 19:00:00', '/images/prezent.jpg', 2, 1, NULL),
    (250.00, 'Korepetycje', 'EXPENSE', 'Lekcje języka angielskiego', '2025-09-07 18:00:00', NULL, 3, 1, NULL),
    (500.00, 'Dochód z freelancingu', 'INCOME', 'Projekt webdevelopment', '2025-09-14 17:00:00', NULL, 1, 2, NULL),
    (140.00, 'Wyjście do kina', 'EXPENSE', 'Film + popcorn', '2025-09-11 20:30:00', '/images/kino.jpg', 2, 1, NULL),
    (90.00, 'Koszt napojów', 'EXPENSE', 'Zakup napojów i przekąsek', '2025-09-18 20:00:00', NULL, 3, 2, NULL),
    (220.00, 'Opłata za wodę', 'EXPENSE', 'Miesięczny rachunek wodny', '2025-09-12 07:30:00', NULL, 1, 3, NULL),
    (30.00, 'Taxi', 'EXPENSE', 'Przejazd taksówką', '2025-09-08 22:00:00', '/images/taxi.jpg', 2, 1, NULL),
    (110.00, 'Naprawa komputera', 'EXPENSE', 'Instalacja nowego dysku SSD', '2025-09-02 16:00:00', '/images/pc.jpg', 3, 3, NULL),
    (1200.00, 'Sprzedaż sprzętu RTV', 'INCOME', 'Sprzedaż starego telewizora', '2025-09-20 10:00:00', NULL, 1, 2, NULL),
    (45.00, 'Opłata za pranie', 'EXPENSE', 'Pranie chemiczne', '2025-09-13 14:00:00', NULL, 2, 1, NULL),
    (55.00, 'Wizyta u dentysty', 'EXPENSE', 'Kontrolny przegląd', '2025-09-04 10:30:00', '/images/dentysta.jpg', 3, 1, NULL),
    (90.00, 'Sprzedaż starych mebli', 'INCOME', 'Skrzynka, stół', '2025-09-05 15:00:00', NULL, 1, 3, NULL),
    (25.00, 'Pizza na wynos', 'EXPENSE', 'Wieczorna pizza', '2025-09-19 18:30:00', '/images/pizza.jpg', 2, 2, NULL),
    (150.00, 'Weekendowy wypad', 'EXPENSE', 'Hotel + posiłki', '2025-09-06 12:00:00', '/images/hotel.jpg', 3, 2, NULL),
    (60.00, 'Kurs online', 'EXPENSE', 'Szkolenie na platformie edukacyjnej', '2025-09-17 19:00:00', NULL, 1, 3, NULL);

