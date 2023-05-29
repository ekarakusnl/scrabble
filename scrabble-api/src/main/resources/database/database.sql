--
-- PostgreSQL database dump
--

-- Dumped from database version 13.8 (Ubuntu 13.8-1.pgdg20.04+1)
-- Dumped by pg_dump version 13.8 (Ubuntu 13.8-1.pgdg20.04+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: actions; Type: TABLE; Schema: public; Owner: scrabble_api
--

CREATE TABLE public.actions (
    id bigint NOT NULL,
    created_date timestamp without time zone NOT NULL,
    last_updated_date timestamp without time zone NOT NULL,
    current_player_number integer,
    game_id bigint,
    game_status character varying(255),
    round_number integer,
    type character varying(255),
    user_id bigint,
    version integer
);


ALTER TABLE public.actions OWNER TO scrabble_api;

--
-- Name: actions_id_seq; Type: SEQUENCE; Schema: public; Owner: scrabble_api
--

ALTER TABLE public.actions ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.actions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: bags; Type: TABLE; Schema: public; Owner: scrabble_api
--

CREATE TABLE public.bags (
    id bigint NOT NULL,
    created_date timestamp without time zone NOT NULL,
    last_updated_date timestamp without time zone NOT NULL,
    enabled integer NOT NULL,
    language character varying(255),
    name character varying(255),
    tile_count integer
);


ALTER TABLE public.bags OWNER TO scrabble_api;

--
-- Name: bags_id_seq; Type: SEQUENCE; Schema: public; Owner: scrabble_api
--

ALTER TABLE public.bags ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.bags_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: boards; Type: TABLE; Schema: public; Owner: scrabble_api
--

CREATE TABLE public.boards (
    id bigint NOT NULL,
    created_date timestamp without time zone NOT NULL,
    last_updated_date timestamp without time zone NOT NULL,
    column_size integer NOT NULL,
    enabled integer NOT NULL,
    name character varying(255) NOT NULL,
    row_size integer NOT NULL
);


ALTER TABLE public.boards OWNER TO scrabble_api;

--
-- Name: boards_id_seq; Type: SEQUENCE; Schema: public; Owner: scrabble_api
--

ALTER TABLE public.boards ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.boards_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: cells; Type: TABLE; Schema: public; Owner: scrabble_api
--

CREATE TABLE public.cells (
    id bigint NOT NULL,
    created_date timestamp without time zone NOT NULL,
    last_updated_date timestamp without time zone NOT NULL,
    board_id bigint NOT NULL,
    cell_number integer,
    center integer NOT NULL,
    color character varying(255),
    column_number integer,
    has_bottom integer NOT NULL,
    has_left integer NOT NULL,
    has_right integer NOT NULL,
    has_top integer NOT NULL,
    letter_value_multiplier integer,
    row_number integer,
    word_score_multiplier integer
);


ALTER TABLE public.cells OWNER TO scrabble_api;

--
-- Name: cells_id_seq; Type: SEQUENCE; Schema: public; Owner: scrabble_api
--

ALTER TABLE public.cells ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.cells_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: chats; Type: TABLE; Schema: public; Owner: scrabble_api
--

CREATE TABLE public.chats (
    id bigint NOT NULL,
    created_date timestamp without time zone NOT NULL,
    last_updated_date timestamp without time zone NOT NULL,
    game_id bigint NOT NULL,
    message character varying(255) NOT NULL,
    user_id bigint NOT NULL
);


ALTER TABLE public.chats OWNER TO scrabble_api;

--
-- Name: chats_id_seq; Type: SEQUENCE; Schema: public; Owner: scrabble_api
--

ALTER TABLE public.chats ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.chats_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: games; Type: TABLE; Schema: public; Owner: scrabble_api
--

CREATE TABLE public.games (
    id bigint NOT NULL,
    created_date timestamp without time zone NOT NULL,
    last_updated_date timestamp without time zone NOT NULL,
    active_player_count integer NOT NULL,
    bag_id bigint NOT NULL,
    board_id bigint NOT NULL,
    current_player_number integer,
    duration integer NOT NULL,
    end_date timestamp without time zone,
    expected_player_count integer NOT NULL,
    name character varying(255) NOT NULL,
    owner_id bigint NOT NULL,
    round_number integer,
    start_date timestamp without time zone,
    status character varying(255) NOT NULL,
    version integer NOT NULL
);


ALTER TABLE public.games OWNER TO scrabble_api;

--
-- Name: games_id_seq; Type: SEQUENCE; Schema: public; Owner: scrabble_api
--

ALTER TABLE public.games ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.games_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: players; Type: TABLE; Schema: public; Owner: scrabble_api
--

CREATE TABLE public.players (
    id bigint NOT NULL,
    created_date timestamp without time zone NOT NULL,
    last_updated_date timestamp without time zone NOT NULL,
    game_id bigint,
    joined_date timestamp without time zone NOT NULL,
    left_date timestamp without time zone,
    player_number integer,
    score integer NOT NULL,
    user_id bigint
);


ALTER TABLE public.players OWNER TO scrabble_api;

--
-- Name: players_id_seq; Type: SEQUENCE; Schema: public; Owner: scrabble_api
--

ALTER TABLE public.players ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.players_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: tiles; Type: TABLE; Schema: public; Owner: scrabble_api
--

CREATE TABLE public.tiles (
    id bigint NOT NULL,
    created_date timestamp without time zone NOT NULL,
    last_updated_date timestamp without time zone NOT NULL,
    bag_id bigint NOT NULL,
    count integer,
    letter character varying(255),
    value integer,
    vowel integer NOT NULL
);


ALTER TABLE public.tiles OWNER TO scrabble_api;

--
-- Name: tiles_id_seq; Type: SEQUENCE; Schema: public; Owner: scrabble_api
--

ALTER TABLE public.tiles ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.tiles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: user_roles; Type: TABLE; Schema: public; Owner: scrabble_api
--

CREATE TABLE public.user_roles (
    id bigint NOT NULL,
    created_date timestamp without time zone NOT NULL,
    last_updated_date timestamp without time zone NOT NULL,
    enabled integer NOT NULL,
    role character varying(255) NOT NULL,
    user_id bigint NOT NULL
);


ALTER TABLE public.user_roles OWNER TO scrabble_api;

--
-- Name: user_roles_id_seq; Type: SEQUENCE; Schema: public; Owner: scrabble_api
--

ALTER TABLE public.user_roles ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.user_roles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: users; Type: TABLE; Schema: public; Owner: scrabble_api
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    created_date timestamp without time zone NOT NULL,
    last_updated_date timestamp without time zone NOT NULL,
    account_non_expired integer NOT NULL,
    account_non_locked integer NOT NULL,
    credentials_non_expired integer NOT NULL,
    email character varying(255) NOT NULL,
    enabled integer NOT NULL,
    password character varying(255) NOT NULL,
    username character varying(255) NOT NULL
);


ALTER TABLE public.users OWNER TO scrabble_api;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: scrabble_api
--

ALTER TABLE public.users ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: words; Type: TABLE; Schema: public; Owner: scrabble_api
--

CREATE TABLE public.words (
    id bigint NOT NULL,
    created_date timestamp without time zone NOT NULL,
    last_updated_date timestamp without time zone NOT NULL,
    action_id bigint NOT NULL,
    game_id bigint NOT NULL,
    round_number integer NOT NULL,
    score integer NOT NULL,
    user_id bigint NOT NULL,
    word character varying(255) NOT NULL
);


ALTER TABLE public.words OWNER TO scrabble_api;

--
-- Name: words_id_seq; Type: SEQUENCE; Schema: public; Owner: scrabble_api
--

ALTER TABLE public.words ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.words_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: actions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: scrabble_api
--

SELECT pg_catalog.setval('public.actions_id_seq', 1, false);


--
-- Name: bags_id_seq; Type: SEQUENCE SET; Schema: public; Owner: scrabble_api
--

SELECT pg_catalog.setval('public.bags_id_seq', 1, false);


--
-- Name: boards_id_seq; Type: SEQUENCE SET; Schema: public; Owner: scrabble_api
--

SELECT pg_catalog.setval('public.boards_id_seq', 1, false);


--
-- Name: cells_id_seq; Type: SEQUENCE SET; Schema: public; Owner: scrabble_api
--

SELECT pg_catalog.setval('public.cells_id_seq', 1, false);


--
-- Name: chats_id_seq; Type: SEQUENCE SET; Schema: public; Owner: scrabble_api
--

SELECT pg_catalog.setval('public.chats_id_seq', 1, false);


--
-- Name: games_id_seq; Type: SEQUENCE SET; Schema: public; Owner: scrabble_api
--

SELECT pg_catalog.setval('public.games_id_seq', 1, false);


--
-- Name: players_id_seq; Type: SEQUENCE SET; Schema: public; Owner: scrabble_api
--

SELECT pg_catalog.setval('public.players_id_seq', 1, false);


--
-- Name: tiles_id_seq; Type: SEQUENCE SET; Schema: public; Owner: scrabble_api
--

SELECT pg_catalog.setval('public.tiles_id_seq', 1, false);


--
-- Name: user_roles_id_seq; Type: SEQUENCE SET; Schema: public; Owner: scrabble_api
--

SELECT pg_catalog.setval('public.user_roles_id_seq', 1, false);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: scrabble_api
--

SELECT pg_catalog.setval('public.users_id_seq', 1, false);


--
-- Name: words_id_seq; Type: SEQUENCE SET; Schema: public; Owner: scrabble_api
--

SELECT pg_catalog.setval('public.words_id_seq', 1, false);


--
-- Name: actions actions_pkey; Type: CONSTRAINT; Schema: public; Owner: scrabble_api
--

ALTER TABLE ONLY public.actions
    ADD CONSTRAINT actions_pkey PRIMARY KEY (id);


--
-- Name: bags bags_pkey; Type: CONSTRAINT; Schema: public; Owner: scrabble_api
--

ALTER TABLE ONLY public.bags
    ADD CONSTRAINT bags_pkey PRIMARY KEY (id);


--
-- Name: boards boards_pkey; Type: CONSTRAINT; Schema: public; Owner: scrabble_api
--

ALTER TABLE ONLY public.boards
    ADD CONSTRAINT boards_pkey PRIMARY KEY (id);


--
-- Name: cells cells_pkey; Type: CONSTRAINT; Schema: public; Owner: scrabble_api
--

ALTER TABLE ONLY public.cells
    ADD CONSTRAINT cells_pkey PRIMARY KEY (id);


--
-- Name: chats chats_pkey; Type: CONSTRAINT; Schema: public; Owner: scrabble_api
--

ALTER TABLE ONLY public.chats
    ADD CONSTRAINT chats_pkey PRIMARY KEY (id);


--
-- Name: games games_pkey; Type: CONSTRAINT; Schema: public; Owner: scrabble_api
--

ALTER TABLE ONLY public.games
    ADD CONSTRAINT games_pkey PRIMARY KEY (id);


--
-- Name: players players_pkey; Type: CONSTRAINT; Schema: public; Owner: scrabble_api
--

ALTER TABLE ONLY public.players
    ADD CONSTRAINT players_pkey PRIMARY KEY (id);


--
-- Name: tiles tiles_pkey; Type: CONSTRAINT; Schema: public; Owner: scrabble_api
--

ALTER TABLE ONLY public.tiles
    ADD CONSTRAINT tiles_pkey PRIMARY KEY (id);


--
-- Name: users uk_user_email; Type: CONSTRAINT; Schema: public; Owner: scrabble_api
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_user_email UNIQUE (email);


--
-- Name: users uk_user_name; Type: CONSTRAINT; Schema: public; Owner: scrabble_api
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_user_name UNIQUE (username);


--
-- Name: user_roles uk_user_role; Type: CONSTRAINT; Schema: public; Owner: scrabble_api
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT uk_user_role UNIQUE (user_id, role, enabled);


--
-- Name: user_roles user_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: scrabble_api
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: scrabble_api
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: words words_pkey; Type: CONSTRAINT; Schema: public; Owner: scrabble_api
--

ALTER TABLE ONLY public.words
    ADD CONSTRAINT words_pkey PRIMARY KEY (id);


--
-- PostgreSQL database dump complete
--
