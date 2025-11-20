CREATE TYPE transaction_type AS ENUM ('expense', 'income');

CREATE TABLE IF NOT EXISTS public.accounts
(
    id bigint NOT NULL,
    name text COLLATE pg_catalog."default" NOT NULL,
    balance numeric(19, 2) NOT NULL,
    is_default boolean NOT NULL,
    currency text COLLATE pg_catalog."default" NOT NULL,
    description text COLLATE pg_catalog."default",
    created_at timestamp without time zone,
    user_id bigint,
    CONSTRAINT accounts_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.categories
(
    id bigint NOT NULL,
    name text COLLATE pg_catalog."default" NOT NULL,
    type text COLLATE pg_catalog."default" NOT NULL,
    icon_path text COLLATE pg_catalog."default",
    is_default boolean NOT NULL,
    user_id bigint,
    CONSTRAINT categories_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.investments
(
    id bigint NOT NULL,
    investment_account bigint NOT NULL,
    symbol text COLLATE pg_catalog."default" NOT NULL,
    name text COLLATE pg_catalog."default" NOT NULL,
    quantity numeric(20, 6) NOT NULL,
    avg_buy_price numeric(20, 6) NOT NULL,
    current_price numeric(20, 6) NOT NULL,
    currency text COLLATE pg_catalog."default" NOT NULL,
    unrealized_pl numeric(20, 6),
    realized_pl numeric(20, 6),
    manual boolean NOT NULL,
    created_at timestamp without time zone NOT NULL,
    CONSTRAINT investments_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.investment_accounts
(
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    name text COLLATE pg_catalog."default" NOT NULL,
    description text COLLATE pg_catalog."default",
    broker_connected boolean NOT NULL,
    auth_token text COLLATE pg_catalog."default",
    last_sync_at timestamp without time zone,
    created_at timestamp without time zone,
    CONSTRAINT investment_accounts_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.recurring_transactions
(
    id bigint NOT NULL,
    amount numeric(19, 2) NOT NULL,
    title text COLLATE pg_catalog."default" NOT NULL,
    type transaction_type NOT NULL,
    description text COLLATE pg_catalog."default",
    start_date timestamp without time zone NOT NULL,
    end_date timestamp without time zone,
    recurring_interval text COLLATE pg_catalog."default" NOT NULL,
    recurring_value int,
    next_occurrence timestamp without time zone,
    is_active boolean NOT NULL,
    created_at timestamp without time zone NOT NULL,
    category_id bigint,
    account_id bigint,
    CONSTRAINT recurring_transactions_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.transactions
(
    id bigint NOT NULL,
    amount numeric(19, 2) NOT NULL,
    title text COLLATE pg_catalog."default" NOT NULL,
    type transaction_type NOT NULL,
    description text COLLATE pg_catalog."default",
    transaction_date timestamp without time zone NOT NULL,
    photo_path text COLLATE pg_catalog."default",
    account_id bigint,
    category_id bigint,
    recurring_transaction_id bigint,
    CONSTRAINT transactions_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.transaction_images
(
    id bigint NOT NULL,
    transaction_id bigint NOT NULL,
    file_name text COLLATE pg_catalog."default" NOT NULL,
    url text COLLATE pg_catalog."default" NOT NULL,
    uploaded_at timestamp without time zone,
    CONSTRAINT transaction_images_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.users
(
    id bigint NOT NULL,
    name text COLLATE pg_catalog."default" NOT NULL,
    surname text COLLATE pg_catalog."default" NOT NULL,
    email text COLLATE pg_catalog."default" NOT NULL,
    password text COLLATE pg_catalog."default" NOT NULL,
    email_last_changed timestamp without time zone,
    CONSTRAINT users_pkey PRIMARY KEY (id)
);

ALTER TABLE IF EXISTS public.accounts
    ADD CONSTRAINT accounts_user_id_fkey FOREIGN KEY (user_id)
    REFERENCES public.users (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE NO ACTION;

ALTER TABLE IF EXISTS public.categories
    ADD CONSTRAINT categories_user_id_fkey FOREIGN KEY (user_id)
    REFERENCES public.users (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE NO ACTION;

ALTER TABLE IF EXISTS public.investments
    ADD CONSTRAINT investments_investment_account_fkey FOREIGN KEY (investment_account)
    REFERENCES public.investment_accounts (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE NO ACTION;

ALTER TABLE IF EXISTS public.investment_accounts
    ADD CONSTRAINT investment_accounts_user_id_fkey FOREIGN KEY (user_id)
    REFERENCES public.users (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE NO ACTION;

ALTER TABLE IF EXISTS public.recurring_transactions
    ADD CONSTRAINT recurring_transactions_account_id_fkey FOREIGN KEY (account_id)
    REFERENCES public.accounts (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE NO ACTION;

ALTER TABLE IF EXISTS public.recurring_transactions
    ADD CONSTRAINT recurring_transactions_category_id_fkey FOREIGN KEY (category_id)
    REFERENCES public.categories (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE NO ACTION;

ALTER TABLE IF EXISTS public.transactions
    ADD CONSTRAINT transactions_account_id_fkey FOREIGN KEY (account_id)
    REFERENCES public.accounts (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE NO ACTION;

ALTER TABLE IF EXISTS public.transactions
    ADD CONSTRAINT transactions_category_id_fkey FOREIGN KEY (category_id)
    REFERENCES public.categories (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE NO ACTION;

ALTER TABLE IF EXISTS public.transactions
    ADD CONSTRAINT transactions_recurring_transaction_id_fkey FOREIGN KEY (recurring_transaction_id)
    REFERENCES public.recurring_transactions (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE NO ACTION
    NOT VALID;

ALTER TABLE IF EXISTS public.transaction_images
    ADD CONSTRAINT transaction_images_transaction_id_fkey FOREIGN KEY (transaction_id)
    REFERENCES public.transactions (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE NO ACTION;
