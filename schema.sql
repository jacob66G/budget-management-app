CREATE TABLE public."user" (
    id BIGINT PRIMARY KEY,
    name TEXT NOT NULL,
    surname TEXT NOT NULL,
    email TEXT NOT NULL,
    password TEXT NOT NULL,
    email_last_changed TIMESTAMP
);

CREATE TABLE public.account (
    id BIGINT PRIMARY KEY,
    name TEXT NOT NULL,
    balance NUMERIC(19,2) NOT NULL,
    is_default BOOLEAN NOT NULL,
    currency TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL,
    user_id BIGINT,
    CONSTRAINT account_user_id_fkey FOREIGN KEY (user_id) REFERENCES public."user"(id)
);

CREATE TABLE public.category (
    id BIGINT PRIMARY KEY,
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    color TEXT NOT NULL,
    icon_path TEXT,
    is_default BOOLEAN NOT NULL,
    user_id BIGINT,
    CONSTRAINT category_user_id_fkey FOREIGN KEY (user_id) REFERENCES public."user"(id)
);

CREATE TABLE public.recurring_transaction (
    id BIGINT PRIMARY KEY,
    amount NUMERIC(19,2) NOT NULL,
    title TEXT NOT NULL,
    description TEXT,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    frequency TEXT NOT NULL,
    recurring_value BIGINT,
    next_occurence TIMESTAMP,
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    category_id BIGINT,
    account_id BIGINT,
    CONSTRAINT recurring_transaction_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.category(id),
    CONSTRAINT recurring_transaction_account_id_fkey FOREIGN KEY (account_id) REFERENCES public.account(id)
);

CREATE TABLE public.transaction (
    id BIGINT PRIMARY KEY,
    amount NUMERIC(19,2) NOT NULL,
    title TEXT NOT NULL,
    description TEXT,
    transaction_date TIMESTAMP NOT NULL,
    photo_path TEXT,
    account_id BIGINT,
    category_id BIGINT,
    recurring_transaction_id BIGINT,
    CONSTRAINT transaction_account_id_fkey FOREIGN KEY (account_id) REFERENCES public.account(id),
    CONSTRAINT transaction_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.category(id),
    CONSTRAINT transaction_recurring_transaction_id_fkey FOREIGN KEY (recurring_transaction_id) REFERENCES public.recurring_transaction(id)
);
