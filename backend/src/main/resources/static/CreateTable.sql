CREATE TABLE IF NOT EXISTS students (
  user_id SERIAL PRIMARY KEY,
  username VARCHAR (50) UNIQUE NOT NULL
);

CREATE TABLE IS NOT EXISTS public."users"
(
    id integer NOT NULL,
    username character varying(50) NOT NULL,
    email character varying(50) NOT NULL,
    password character varying(50) NOT NULL,
    PRIMARY KEY (id)
);