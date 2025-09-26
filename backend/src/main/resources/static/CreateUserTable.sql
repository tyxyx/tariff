Table: public.users

DROP TABLE IF EXISTS public.users;

CREATE TABLE IF NOT EXISTS public.users
(
    email character varying(255) COLLATE pg_catalog."default" PRIMARY KEY,
    password character varying(255) COLLATE pg_catalog."default" NOT NULL,
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.users
    OWNER to postgres;