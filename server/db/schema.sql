CREATE TABLE IF NOT EXISTS public.account
(
    id bigint NOT NULL,
    name text COLLATE pg_catalog."default" NOT NULL,
    balance numeric(19, 2) NOT NULL,
    is_default boolean NOT NULL,
    currency text COLLATE pg_catalog."default" NOT NULL,
    description text COLLATE pg_catalog."default",
    created_at timestamp without time zone,
    user_id bigint,
    CONSTRAINT account_pkey PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS public.category
(
    id bigint NOT NULL,
    name text COLLATE pg_catalog."default" NOT NULL,
    type text COLLATE pg_catalog."default" NOT NULL,
    icon_path text COLLATE pg_catalog."default",
    is_default boolean NOT NULL,
    user_id bigint,
    CONSTRAINT category_pkey PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS public.investment
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
    CONSTRAINT investment_pkey PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS public.investment_account
(
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    name text COLLATE pg_catalog."default" NOT NULL,
    description text COLLATE pg_catalog."default",
    broker_connected boolean NOT NULL,
    auth_token text COLLATE pg_catalog."default",
    last_sync_at timestamp without time zone,
    created_at timestamp without time zone,
    CONSTRAINT investment_account_pkey PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS public.recurring_transaction
(
    id bigint NOT NULL,
    amount numeric(19, 2) NOT NULL,
    title text COLLATE pg_catalog."default" NOT NULL,
    description text COLLATE pg_catalog."default",
    start_date timestamp without time zone NOT NULL,
    end_date timestamp without time zone,
    frequency text COLLATE pg_catalog."default" NOT NULL,
    recurring_value bigint,
    next_occurence timestamp without time zone,
    is_active boolean NOT NULL,
    created_at timestamp without time zone NOT NULL,
    category_id bigint,
    account_id bigint,
    CONSTRAINT recurring_transaction_pkey PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS public.transaction
(
    id bigint NOT NULL,
    amount numeric(19, 2) NOT NULL,
    title text COLLATE pg_catalog."default" NOT NULL,
    description text COLLATE pg_catalog."default",
    transaction_date timestamp without time zone NOT NULL,
    photo_path text COLLATE pg_catalog."default",
    account_id bigint,
    category_id bigint,
    recurring_transaction_id bigint,
    CONSTRAINT transaction_pkey PRIMARY KEY (id)
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

CREATE TABLE IF NOT EXISTS public."user"
(
    id bigint NOT NULL,
    name text COLLATE pg_catalog."default" NOT NULL,
    surname text COLLATE pg_catalog."default" NOT NULL,
    email text COLLATE pg_catalog."default" NOT NULL,
    password text COLLATE pg_catalog."default" NOT NULL,
    email_last_changed timestamp without time zone,
    CONSTRAINT user_pkey PRIMARY KEY (id)
    );

ALTER TABLE IF EXISTS public.account
    ADD CONSTRAINT account_user_id_fkey FOREIGN KEY (user_id)
    REFERENCES public."user" (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE NO ACTION;


ALTER TABLE IF EXISTS public.category
    ADD CONSTRAINT category_user_id_fkey FOREIGN KEY (user_id)
    REFERENCES public."user" (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE NO ACTION;


ALTER TABLE IF EXISTS public.investment
    ADD CONSTRAINT investment_investment_account_fkey FOREIGN KEY (investment_account)
    REFERENCES public.investment_account (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE NO ACTION;


ALTER TABLE IF EXISTS public.investment_account
    ADD CONSTRAINT investment_account_user_id_fkey FOREIGN KEY (user_id)
    REFERENCES public."user" (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE NO ACTION;


ALTER TABLE IF EXISTS public.recurring_transaction
    ADD CONSTRAINT recurring_transaction_account_id_fkey FOREIGN KEY (account_id)
    REFERENCES public.account (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE NO ACTION;


ALTER TABLE IF EXISTS public.recurring_transaction
    ADD CONSTRAINT recurring_transaction_category_id_fkey FOREIGN KEY (category_id)
    REFERENCES public.category (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE NO ACTION;


ALTER TABLE IF EXISTS public.transaction
    ADD CONSTRAINT transaction_account_id_fkey FOREIGN KEY (account_id)
    REFERENCES public.account (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE NO ACTION;


ALTER TABLE IF EXISTS public.transaction
    ADD CONSTRAINT transaction_category_id_fkey FOREIGN KEY (category_id)
    REFERENCES public.category (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE NO ACTION;


ALTER TABLE IF EXISTS public.transaction
    ADD CONSTRAINT transaction_recurring_transaction_id_fkey FOREIGN KEY (recurring_transaction_id)
    REFERENCES public.recurring_transaction (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE NO ACTION
    NOT VALID;


ALTER TABLE IF EXISTS public.transaction_images
    ADD CONSTRAINT transaction_images_transaction_id_fkey FOREIGN KEY (transaction_id)
    REFERENCES public.transaction (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE NO ACTION;