--
-- PostgreSQL database dump
--

-- Dumped from database version 15.1 (Debian 15.1-1.pgdg110+1)
-- Dumped by pg_dump version 15.1 (Debian 15.1-1.pgdg110+1)

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

--
-- Name: check_max_cards_per_user(); Type: FUNCTION; Schema: public; Owner: josip
--

CREATE FUNCTION public.check_max_cards_per_user() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF (SELECT COUNT(*) FROM deck WHERE fk_userId = NEW.fk_userId) > 4 THEN
        RAISE EXCEPTION 'Cannot have more than 4 cards per user';
    END IF;
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.check_max_cards_per_user() OWNER TO josip;

--
-- Name: check_package_card_limit(); Type: FUNCTION; Schema: public; Owner: josip
--

CREATE FUNCTION public.check_package_card_limit() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF (SELECT COUNT(*) FROM card WHERE fk_packId = NEW.fk_packId) > 5 THEN
        RAISE EXCEPTION 'A package can only contain 5 cards';
    END IF;
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.check_package_card_limit() OWNER TO josip;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: battle; Type: TABLE; Schema: public; Owner: josip
--

CREATE TABLE public.battle (
    id character varying NOT NULL,
    fk_player1id character varying,
    fk_player2id character varying,
    battlelog character varying COLLATE pg_catalog."C",
    battleoutcome character varying,
    created_at timestamp without time zone DEFAULT now()
);


ALTER TABLE public.battle OWNER TO josip;

--
-- Name: card; Type: TABLE; Schema: public; Owner: josip
--

CREATE TABLE public.card (
    id character varying NOT NULL,
    name character varying NOT NULL COLLATE pg_catalog."C",
    damage double precision NOT NULL,
    elementtype character varying,
    cardtype character varying,
    fk_ownerid character varying,
    fk_packid character varying
);


ALTER TABLE public.card OWNER TO josip;

--
-- Name: deck; Type: TABLE; Schema: public; Owner: josip
--

CREATE TABLE public.deck (
    fk_userid character varying NOT NULL,
    fk_cardid character varying NOT NULL
);


ALTER TABLE public.deck OWNER TO josip;

--
-- Name: package; Type: TABLE; Schema: public; Owner: josip
--

CREATE TABLE public.package (
    id character varying NOT NULL,
    price integer,
    fk_userid character varying
);


ALTER TABLE public.package OWNER TO josip;

--
-- Name: stat; Type: TABLE; Schema: public; Owner: josip
--

CREATE TABLE public.stat (
    fk_userid character varying NOT NULL,
    elo integer,
    wins integer,
    draws integer,
    total integer
);


ALTER TABLE public.stat OWNER TO josip;

--
-- Name: trade; Type: TABLE; Schema: public; Owner: josip
--

CREATE TABLE public.trade (
    id character varying NOT NULL,
    fk_cardtotradeid character varying,
    cardtype character varying,
    minimumdamage double precision,
    fk_user1id character varying,
    fk_user2id character varying
);


ALTER TABLE public.trade OWNER TO josip;

--
-- Name: user; Type: TABLE; Schema: public; Owner: josip
--

CREATE TABLE public."user" (
    id character varying NOT NULL,
    passwordhash character varying NOT NULL,
    coins integer,
    username character varying NOT NULL COLLATE pg_catalog."C",
    name character varying COLLATE pg_catalog."C",
    bio character varying COLLATE pg_catalog."C",
    image character varying COLLATE pg_catalog."C"
);


ALTER TABLE public."user" OWNER TO josip;

--
-- Data for Name: battle; Type: TABLE DATA; Schema: public; Owner: josip
--

COPY public.battle (id, fk_player1id, fk_player2id, battlelog, battleoutcome, created_at) FROM stdin;
e02cc8a8-b90b-4daa-8b06-cf41cf5bb895	2b053bd2-292f-46d0-9051-16c15f9ed638	0ea3d52d-3acf-4026-a584-0b872db6c4b8	=====================================\nLet's Duel, kienboec [Elo: 100] vs. altenhof [Elo: 100]\n\naltenhof's "WaterSpell" [22] WINS against kienboec's "WaterSpell" [20]\naltenhof's "Knight" [20] WINS against kienboec's "WaterGoblin" [10]\nkienboec's "RegularSpell" [56] WINS against altenhof's "WaterGoblin" [5]\nkienboec's "Dragon" [50] DRAWS against altenhof's "RegularSpell" [50]\nkienboec's "RegularSpell" [56] WINS against altenhof's "WaterSpell" [11]\nkienboec's "Dragon" [50] WINS against altenhof's "Knight" [20]\naltenhof's "RegularSpell" [50] WINS against kienboec's "Knight" [20]\nkienboec's "Dragon" [50] DRAWS against altenhof's "RegularSpell" [50]\nkienboec's "RegularSpell" [56] WINS against altenhof's "WaterSpell" [10]\naltenhof's "RegularSpell" [100] WINS against kienboec's "WaterGoblin" [5]\naltenhof's "RegularSpell" [45] WINS against kienboec's "RegularSpell" [28]\nkienboec's "WaterSpell" WINS against altenhof's "Knight" because armor of knights is so heavy that water spells make them drown them instantly.\naltenhof's "RegularSpell" [90] WINS against kienboec's "WaterSpell" [10]\naltenhof's "RegularSpell" [45] WINS against kienboec's "Knight" [20]\nkienboec's "Dragon" [50] WINS against altenhof's "RegularSpell" [28]\nkienboec's "WaterSpell" WINS against altenhof's "Knight" because armor of knights is so heavy that water spells make them drown them instantly.\nkienboec's "Dragon" WINS against altenhof's "WaterGoblin" because the goblin was too afraid to attack the dragon.\nkienboec's "Dragon" [50] DRAWS against altenhof's "RegularSpell" [50]\naltenhof's "RegularSpell" [50] WINS against kienboec's "Knight" [20]\nkienboec's "Dragon" [50] WINS against altenhof's "Knight" [20]\nkienboec's "Dragon" [100] WINS against altenhof's "WaterSpell" [10]\naltenhof's "RegularSpell" [100] WINS against kienboec's "WaterSpell" [11]\naltenhof's "WaterSpell" [22] WINS against kienboec's "WaterGoblin" [10]\nkienboec's "RegularSpell" [56] WINS against altenhof's "WaterGoblin" [5]\naltenhof's "WaterSpell" [22] WINS against kienboec's "WaterSpell" [20]\naltenhof's "WaterSpell" WINS against kienboec's "Knight" because armor of knights is so heavy that water spells make them drown them instantly.\nkienboec's "RegularSpell" [56] WINS against altenhof's "WaterSpell" [10]\naltenhof's "RegularSpell" [90] WINS against kienboec's "WaterSpell" [10]\naltenhof's "WaterSpell" [22] WINS against kienboec's "WaterGoblin" [10]\naltenhof's "RegularSpell" [50] WINS against kienboec's "RegularSpell" [28]\nkienboec's "Dragon" [100] WINS against altenhof's "WaterSpell" [10]\naltenhof's "RegularSpell" [56] WINS against kienboec's "WaterSpell" [10]\nkienboec's "Dragon" WINS against altenhof's "WaterGoblin" because the goblin was too afraid to attack the dragon.\naltenhof's "RegularSpell" [100] WINS against kienboec's "WaterGoblin" [5]\nkienboec's "Dragon" WINS against altenhof's "WaterGoblin" because the goblin was too afraid to attack the dragon.\naltenhof's "RegularSpell" [90] WINS against kienboec's "WaterGoblin" [5]\nkienboec's "Dragon" [50] WINS against altenhof's "RegularSpell" [28]\nkienboec's "RegularSpell" [28] WINS against altenhof's "Knight" [20]\naltenhof's "WaterSpell" WINS against kienboec's "Knight" because armor of knights is so heavy that water spells make them drown them instantly.\nkienboec's "Dragon" [50] WINS against altenhof's "Knight" [30!]\nkienboec's "Dragon" [100] WINS against altenhof's "WaterSpell" [11]\naltenhof's "RegularSpell" [50] WINS against kienboec's "Knight" [20]\naltenhof's "RegularSpell" [90] WINS against kienboec's "WaterSpell" [11]\naltenhof's "RegularSpell" [45] WINS against kienboec's "RegularSpell" [28]\nkienboec's "Dragon" WINS against altenhof's "WaterGoblin" because the goblin was too afraid to attack the dragon.\nkienboec's "Dragon" [50] DRAWS against altenhof's "RegularSpell" [50]\nkienboec's "Dragon" [50] WINS against altenhof's "RegularSpell" [28]\nkienboec's "Dragon" [100] WINS against altenhof's "WaterSpell" [11]\naltenhof's "WaterSpell" [20] WINS against kienboec's "WaterGoblin" [10]\naltenhof's "RegularSpell" [90] WINS against kienboec's "WaterSpell" [11]\nkienboec's "RegularSpell" [28] WINS against altenhof's "Knight" [20]\naltenhof's "WaterSpell" WINS against kienboec's "Knight" because armor of knights is so heavy that water spells make them drown them instantly.\nkienboec's "Dragon" [50] DRAWS against altenhof's "RegularSpell" [50]\nkienboec's "Dragon" WINS against altenhof's "WaterGoblin" because the goblin was too afraid to attack the dragon.\nkienboec's "Dragon" [50] DRAWS against altenhof's "RegularSpell" [50]\naltenhof's "Knight" [20] DRAWS against kienboec's "Dragon" [50] by narrowly escaping the attack\naltenhof's "RegularSpell" [45] WINS against kienboec's "RegularSpell" [28]\naltenhof's "RegularSpell" [90] WINS against kienboec's "WaterGoblin" [5]\nkienboec's "Dragon" [50] WINS against altenhof's "Knight" [20]\naltenhof's "RegularSpell" [28] WINS against kienboec's "Knight" [20]\nkienboec's "Dragon" [50] WINS against altenhof's "RegularSpell" [28]\nkienboec's "Dragon" [50] WINS against altenhof's "Knight" [20]\nkienboec's "Knight" [20] WINS against altenhof's "WaterGoblin" [10]\naltenhof's "RegularSpell" [90] WINS against kienboec's "WaterGoblin" [5]\nkienboec's "Dragon" WINS against altenhof's "WaterGoblin" because the goblin was too afraid to attack the dragon.\naltenhof's "RegularSpell" [45] WINS against kienboec's "RegularSpell" [28]\nkienboec's "Dragon" [50] WINS against altenhof's "RegularSpell" [28]\nkienboec's "Dragon" [50] WINS against altenhof's "RegularSpell" [45]\naltenhof's "WaterSpell" [20] WINS against kienboec's "WaterGoblin" [10]\naltenhof's "RegularSpell" [50] WINS against kienboec's "Knight" [20]\nkienboec's "Dragon" [100] WINS against altenhof's "WaterSpell" [10]\nkienboec's "RegularSpell" [56] WINS against altenhof's "WaterSpell" [11]\nkienboec's "WaterSpell" WINS against altenhof's "Knight" because armor of knights is so heavy that water spells make them drown them instantly.\naltenhof's "RegularSpell" [100] WINS against kienboec's "WaterSpell" [10]\nkienboec's "Dragon" [50] DRAWS against altenhof's "RegularSpell" [50]\naltenhof's "RegularSpell" [50] WINS against kienboec's "RegularSpell" [45]\naltenhof's "WaterSpell" WINS against kienboec's "Knight" because armor of knights is so heavy that water spells make them drown them instantly.\nkienboec's "Dragon" [50] DRAWS against altenhof's "RegularSpell" [50]\nkienboec's "WaterSpell" WINS against altenhof's "Knight" because armor of knights is so heavy that water spells make them drown them instantly.\nkienboec's "Dragon" [100] WINS against altenhof's "WaterSpell" [10]\naltenhof's "RegularSpell" [50] WINS against kienboec's "Knight" [20]\nkienboec's "Dragon" [50] WINS against altenhof's "RegularSpell" [45]\nkienboec's "RegularSpell" [56] WINS against altenhof's "WaterGoblin" [5]\naltenhof's "Knight" [20] WINS against kienboec's "WaterGoblin" [15!]\nkienboec's "WaterSpell" WINS against altenhof's "Knight" because armor of knights is so heavy that water spells make them drown them instantly.\nkienboec's "RegularSpell" [56] WINS against altenhof's "WaterGoblin" [5]\naltenhof's "RegularSpell" [50] WINS against kienboec's "RegularSpell" [28]\nkienboec's "Dragon" [50] DRAWS against altenhof's "RegularSpell" [50]\nkienboec's "Dragon" [50] DRAWS against altenhof's "RegularSpell" [50]\naltenhof's "RegularSpell" [56] WINS against kienboec's "WaterSpell" [10]\naltenhof's "RegularSpell" [50] WINS against kienboec's "Knight" [20]\naltenhof's "RegularSpell" [56] WINS against kienboec's "WaterSpell" [11]\naltenhof's "Knight" [30!] WINS against kienboec's "WaterGoblin" [10]\nkienboec's "Dragon" [50] WINS against altenhof's "RegularSpell" [28]\naltenhof's "RegularSpell" [50] WINS against kienboec's "RegularSpell" [28]\nkienboec's "RegularSpell" [45] WINS against altenhof's "RegularSpell" [28]\nkienboec's "RegularSpell" [56] WINS against altenhof's "WaterSpell" [11]\nkienboec's "RegularSpell" [56] WINS against altenhof's "WaterSpell" [10]\naltenhof's "RegularSpell" [50] WINS against kienboec's "RegularSpell" [28]\nkienboec's "Dragon" WINS against altenhof's "WaterGoblin" because the goblin was too afraid to attack the dragon.\n\nkienboec [Elo: 100] DRAWS AGAINST altenhof [Elo: 100]\n=====================================\n	DRAW	2023-01-08 01:57:38.5492
\.


--
-- Data for Name: card; Type: TABLE DATA; Schema: public; Owner: josip
--

COPY public.card (id, name, damage, elementtype, cardtype, fk_ownerid, fk_packid) FROM stdin;
845f0dc7-37d0-426e-994e-43fc3ac83c08	WaterGoblin	10	WATER	MONSTER	2b053bd2-292f-46d0-9051-16c15f9ed638	b7fb04c0-e5ea-4531-9430-ffd04bc6e141
99f8f8dc-e25e-4a95-aa2c-782823f36e2a	Dragon	50	NORMAL	MONSTER	2b053bd2-292f-46d0-9051-16c15f9ed638	b7fb04c0-e5ea-4531-9430-ffd04bc6e141
e85e3976-7c86-4d06-9a80-641c2019a79f	WaterSpell	20	WATER	SPELL	2b053bd2-292f-46d0-9051-16c15f9ed638	b7fb04c0-e5ea-4531-9430-ffd04bc6e141
dfdd758f-649c-40f9-ba3a-8657f4b3439f	FireSpell	25	FIRE	SPELL	2b053bd2-292f-46d0-9051-16c15f9ed638	b7fb04c0-e5ea-4531-9430-ffd04bc6e141
644808c2-f87a-4600-b313-122b02322fd5	WaterGoblin	9	WATER	MONSTER	2b053bd2-292f-46d0-9051-16c15f9ed638	c146556f-59c7-4133-a229-18f963b5c2a8
4a2757d6-b1c3-47ac-b9a3-91deab093531	Dragon	55	NORMAL	MONSTER	2b053bd2-292f-46d0-9051-16c15f9ed638	c146556f-59c7-4133-a229-18f963b5c2a8
91a6471b-1426-43f6-ad65-6fc473e16f9f	WaterSpell	21	WATER	SPELL	2b053bd2-292f-46d0-9051-16c15f9ed638	c146556f-59c7-4133-a229-18f963b5c2a8
4ec8b269-0dfa-4f97-809a-2c63fe2a0025	Ork	55	NORMAL	MONSTER	2b053bd2-292f-46d0-9051-16c15f9ed638	c146556f-59c7-4133-a229-18f963b5c2a8
f8043c23-1534-4487-b66b-238e0c3c39b5	WaterSpell	23	WATER	SPELL	2b053bd2-292f-46d0-9051-16c15f9ed638	c146556f-59c7-4133-a229-18f963b5c2a8
b017ee50-1c14-44e2-bfd6-2c0c5653a37c	WaterGoblin	11	WATER	MONSTER	2b053bd2-292f-46d0-9051-16c15f9ed638	354b7563-af98-4294-8f54-7e3b26282878
d04b736a-e874-4137-b191-638e0ff3b4e7	Dragon	70	NORMAL	MONSTER	2b053bd2-292f-46d0-9051-16c15f9ed638	354b7563-af98-4294-8f54-7e3b26282878
88221cfe-1f84-41b9-8152-8e36c6a354de	WaterSpell	22	WATER	SPELL	2b053bd2-292f-46d0-9051-16c15f9ed638	354b7563-af98-4294-8f54-7e3b26282878
1d3f175b-c067-4359-989d-96562bfa382c	Ork	40	NORMAL	MONSTER	2b053bd2-292f-46d0-9051-16c15f9ed638	354b7563-af98-4294-8f54-7e3b26282878
171f6076-4eb5-4a7d-b3f2-2d650cc3d237	RegularSpell	28	NORMAL	SPELL	2b053bd2-292f-46d0-9051-16c15f9ed638	354b7563-af98-4294-8f54-7e3b26282878
ed1dc1bc-f0aa-4a0c-8d43-1402189b33c8	WaterGoblin	10	WATER	MONSTER	2b053bd2-292f-46d0-9051-16c15f9ed638	e44de49a-2806-4fea-9ead-ce75c36fec01
65ff5f23-1e70-4b79-b3bd-f6eb679dd3b5	Dragon	50	NORMAL	MONSTER	2b053bd2-292f-46d0-9051-16c15f9ed638	e44de49a-2806-4fea-9ead-ce75c36fec01
55ef46c4-016c-4168-bc43-6b9b1e86414f	WaterSpell	20	WATER	SPELL	2b053bd2-292f-46d0-9051-16c15f9ed638	e44de49a-2806-4fea-9ead-ce75c36fec01
f3fad0f2-a1af-45df-b80d-2e48825773d9	Ork	45	NORMAL	MONSTER	2b053bd2-292f-46d0-9051-16c15f9ed638	e44de49a-2806-4fea-9ead-ce75c36fec01
8c20639d-6400-4534-bd0f-ae563f11f57a	WaterSpell	25	WATER	SPELL	2b053bd2-292f-46d0-9051-16c15f9ed638	e44de49a-2806-4fea-9ead-ce75c36fec01
d7d0cb94-2cbf-4f97-8ccf-9933dc5354b8	WaterGoblin	9	WATER	MONSTER	0ea3d52d-3acf-4026-a584-0b872db6c4b8	9ee9d957-9e58-4594-8549-cdc8d772d831
44c82fbc-ef6d-44ab-8c7a-9fb19a0e7c6e	Dragon	55	NORMAL	MONSTER	0ea3d52d-3acf-4026-a584-0b872db6c4b8	9ee9d957-9e58-4594-8549-cdc8d772d831
2c98cd06-518b-464c-b911-8d787216cddd	WaterSpell	21	WATER	SPELL	0ea3d52d-3acf-4026-a584-0b872db6c4b8	9ee9d957-9e58-4594-8549-cdc8d772d831
dcd93250-25a7-4dca-85da-cad2789f7198	FireSpell	23	FIRE	SPELL	0ea3d52d-3acf-4026-a584-0b872db6c4b8	9ee9d957-9e58-4594-8549-cdc8d772d831
b2237eca-0271-43bd-87f6-b22f70d42ca4	WaterGoblin	11	WATER	MONSTER	0ea3d52d-3acf-4026-a584-0b872db6c4b8	0e723428-d15f-4de3-b68d-c075451c316b
9e8238a4-8a7a-487f-9f7d-a8c97899eb48	Dragon	70	NORMAL	MONSTER	0ea3d52d-3acf-4026-a584-0b872db6c4b8	0e723428-d15f-4de3-b68d-c075451c316b
d60e23cf-2238-4d49-844f-c7589ee5342e	WaterSpell	22	WATER	SPELL	0ea3d52d-3acf-4026-a584-0b872db6c4b8	0e723428-d15f-4de3-b68d-c075451c316b
fc305a7a-36f7-4d30-ad27-462ca0445649	Ork	40	NORMAL	MONSTER	0ea3d52d-3acf-4026-a584-0b872db6c4b8	0e723428-d15f-4de3-b68d-c075451c316b
84d276ee-21ec-4171-a509-c1b88162831c	RegularSpell	28	NORMAL	SPELL	0ea3d52d-3acf-4026-a584-0b872db6c4b8	0e723428-d15f-4de3-b68d-c075451c316b
2272ba48-6662-404d-a9a1-41a9bed316d9	WaterGoblin	11	WATER	MONSTER	\N	6a203f8a-e8e2-4286-af38-a576466f4176
3871d45b-b630-4a0d-8bc6-a5fc56b6a043	Dragon	70	NORMAL	MONSTER	\N	6a203f8a-e8e2-4286-af38-a576466f4176
166c1fd5-4dcb-41a8-91cb-f45dcd57cef3	Knight	22	NORMAL	MONSTER	\N	6a203f8a-e8e2-4286-af38-a576466f4176
237dbaef-49e3-4c23-b64b-abf5c087b276	WaterSpell	40	WATER	SPELL	\N	6a203f8a-e8e2-4286-af38-a576466f4176
27051a20-8580-43ff-a473-e986b52f297a	FireElf	28	FIRE	MONSTER	\N	6a203f8a-e8e2-4286-af38-a576466f4176
67f9048f-99b8-4ae4-b866-d8008d00c53d	WaterGoblin	10	WATER	MONSTER	0ea3d52d-3acf-4026-a584-0b872db6c4b8	ae8ec99d-c409-4fe1-ac4e-547c4bd86706
aa9999a0-734c-49c6-8f4a-651864b14e62	RegularSpell	50	NORMAL	SPELL	0ea3d52d-3acf-4026-a584-0b872db6c4b8	ae8ec99d-c409-4fe1-ac4e-547c4bd86706
d6e9c720-9b5a-40c7-a6b2-bc34752e3463	Knight	20	NORMAL	MONSTER	0ea3d52d-3acf-4026-a584-0b872db6c4b8	ae8ec99d-c409-4fe1-ac4e-547c4bd86706
70962948-2bf7-44a9-9ded-8c68eeac7793	WaterGoblin	9	WATER	MONSTER	0ea3d52d-3acf-4026-a584-0b872db6c4b8	241f6f7e-6c17-4bf8-922d-bff85cac264a
74635fae-8ad3-4295-9139-320ab89c2844	FireSpell	55	FIRE	SPELL	0ea3d52d-3acf-4026-a584-0b872db6c4b8	241f6f7e-6c17-4bf8-922d-bff85cac264a
ce6bcaee-47e1-4011-a49e-5a4d7d4245f3	Knight	21	NORMAL	MONSTER	0ea3d52d-3acf-4026-a584-0b872db6c4b8	241f6f7e-6c17-4bf8-922d-bff85cac264a
a6fde738-c65a-4b10-b400-6fef0fdb28ba	FireSpell	55	FIRE	SPELL	0ea3d52d-3acf-4026-a584-0b872db6c4b8	241f6f7e-6c17-4bf8-922d-bff85cac264a
1cb6ab86-bdb2-47e5-b6e4-68c5ab389334	Ork	45	NORMAL	MONSTER	0ea3d52d-3acf-4026-a584-0b872db6c4b8	b7fb04c0-e5ea-4531-9430-ffd04bc6e141
951e886a-0fbf-425d-8df5-af2ee4830d85	Ork	55	NORMAL	MONSTER	2b053bd2-292f-46d0-9051-16c15f9ed638	9ee9d957-9e58-4594-8549-cdc8d772d831
02a9c76e-b17d-427f-9240-2dd49b0d3bfd	RegularSpell	45	NORMAL	SPELL	0ea3d52d-3acf-4026-a584-0b872db6c4b8	ae8ec99d-c409-4fe1-ac4e-547c4bd86706
2508bf5c-20d7-43b4-8c77-bc677decadef	FireElf	25	FIRE	MONSTER	0ea3d52d-3acf-4026-a584-0b872db6c4b8	ae8ec99d-c409-4fe1-ac4e-547c4bd86706
a1618f1e-4f4c-4e09-9647-87e16f1edd2d	FireElf	23	FIRE	MONSTER	0ea3d52d-3acf-4026-a584-0b872db6c4b8	241f6f7e-6c17-4bf8-922d-bff85cac264a
\.


--
-- Data for Name: deck; Type: TABLE DATA; Schema: public; Owner: josip
--

COPY public.deck (fk_userid, fk_cardid) FROM stdin;
2b053bd2-292f-46d0-9051-16c15f9ed638	845f0dc7-37d0-426e-994e-43fc3ac83c08
2b053bd2-292f-46d0-9051-16c15f9ed638	99f8f8dc-e25e-4a95-aa2c-782823f36e2a
2b053bd2-292f-46d0-9051-16c15f9ed638	e85e3976-7c86-4d06-9a80-641c2019a79f
2b053bd2-292f-46d0-9051-16c15f9ed638	171f6076-4eb5-4a7d-b3f2-2d650cc3d237
0ea3d52d-3acf-4026-a584-0b872db6c4b8	d60e23cf-2238-4d49-844f-c7589ee5342e
0ea3d52d-3acf-4026-a584-0b872db6c4b8	aa9999a0-734c-49c6-8f4a-651864b14e62
0ea3d52d-3acf-4026-a584-0b872db6c4b8	d6e9c720-9b5a-40c7-a6b2-bc34752e3463
0ea3d52d-3acf-4026-a584-0b872db6c4b8	02a9c76e-b17d-427f-9240-2dd49b0d3bfd
\.


--
-- Data for Name: package; Type: TABLE DATA; Schema: public; Owner: josip
--

COPY public.package (id, price, fk_userid) FROM stdin;
b7fb04c0-e5ea-4531-9430-ffd04bc6e141	5	2b053bd2-292f-46d0-9051-16c15f9ed638
c146556f-59c7-4133-a229-18f963b5c2a8	5	2b053bd2-292f-46d0-9051-16c15f9ed638
354b7563-af98-4294-8f54-7e3b26282878	5	2b053bd2-292f-46d0-9051-16c15f9ed638
e44de49a-2806-4fea-9ead-ce75c36fec01	5	2b053bd2-292f-46d0-9051-16c15f9ed638
9ee9d957-9e58-4594-8549-cdc8d772d831	5	0ea3d52d-3acf-4026-a584-0b872db6c4b8
0e723428-d15f-4de3-b68d-c075451c316b	5	0ea3d52d-3acf-4026-a584-0b872db6c4b8
6a203f8a-e8e2-4286-af38-a576466f4176	5	\N
ae8ec99d-c409-4fe1-ac4e-547c4bd86706	5	0ea3d52d-3acf-4026-a584-0b872db6c4b8
241f6f7e-6c17-4bf8-922d-bff85cac264a	5	0ea3d52d-3acf-4026-a584-0b872db6c4b8
\.


--
-- Data for Name: stat; Type: TABLE DATA; Schema: public; Owner: josip
--

COPY public.stat (fk_userid, elo, wins, draws, total) FROM stdin;
800c4e76-d96d-470a-9991-478c19f9afdd	\N	\N	\N	\N
2b053bd2-292f-46d0-9051-16c15f9ed638	100	0	1	1
0ea3d52d-3acf-4026-a584-0b872db6c4b8	100	0	1	1
\.


--
-- Data for Name: trade; Type: TABLE DATA; Schema: public; Owner: josip
--

COPY public.trade (id, fk_cardtotradeid, cardtype, minimumdamage, fk_user1id, fk_user2id) FROM stdin;
6cd85277-4590-49d4-b0cf-ba0a921faad0	1cb6ab86-bdb2-47e5-b6e4-68c5ab389334	MONSTER	15	2b053bd2-292f-46d0-9051-16c15f9ed638	0ea3d52d-3acf-4026-a584-0b872db6c4b8
\.


--
-- Data for Name: user; Type: TABLE DATA; Schema: public; Owner: josip
--

COPY public."user" (id, passwordhash, coins, username, name, bio, image) FROM stdin;
800c4e76-d96d-470a-9991-478c19f9afdd	175490a3844460fe5b3d03b2879863a81f812c7566165e16673fa96f0b0c5bd8	20	admin	\N	\N	\N
2b053bd2-292f-46d0-9051-16c15f9ed638	bd3dae5fb91f88a4f0978222dfd58f59a124257cb081486387cbae9df11fb879	0	kienboec	Kienboeck	me playin...	:-)
0ea3d52d-3acf-4026-a584-0b872db6c4b8	24d9c7b19834aa278ce9609a00d156f61bd83184c2634d1c2b1d6aaa9c235b3a	0	altenhof	Altenhofer	me codin...	:-D
\.


--
-- Name: battle battle_pkey; Type: CONSTRAINT; Schema: public; Owner: josip
--

ALTER TABLE ONLY public.battle
    ADD CONSTRAINT battle_pkey PRIMARY KEY (id);


--
-- Name: card card_pkey; Type: CONSTRAINT; Schema: public; Owner: josip
--

ALTER TABLE ONLY public.card
    ADD CONSTRAINT card_pkey PRIMARY KEY (id);


--
-- Name: deck deck_pkey; Type: CONSTRAINT; Schema: public; Owner: josip
--

ALTER TABLE ONLY public.deck
    ADD CONSTRAINT deck_pkey PRIMARY KEY (fk_cardid, fk_userid);


--
-- Name: package package_pkey; Type: CONSTRAINT; Schema: public; Owner: josip
--

ALTER TABLE ONLY public.package
    ADD CONSTRAINT package_pkey PRIMARY KEY (id);


--
-- Name: stat stat_pkey; Type: CONSTRAINT; Schema: public; Owner: josip
--

ALTER TABLE ONLY public.stat
    ADD CONSTRAINT stat_pkey PRIMARY KEY (fk_userid);


--
-- Name: trade trade_pkey; Type: CONSTRAINT; Schema: public; Owner: josip
--

ALTER TABLE ONLY public.trade
    ADD CONSTRAINT trade_pkey PRIMARY KEY (id);


--
-- Name: user user_pkey; Type: CONSTRAINT; Schema: public; Owner: josip
--

ALTER TABLE ONLY public."user"
    ADD CONSTRAINT user_pkey PRIMARY KEY (id);


--
-- Name: user_username_uindex; Type: INDEX; Schema: public; Owner: josip
--

CREATE UNIQUE INDEX user_username_uindex ON public."user" USING btree (username);


--
-- Name: card check_package_card_limit; Type: TRIGGER; Schema: public; Owner: josip
--

CREATE TRIGGER check_package_card_limit BEFORE INSERT ON public.card FOR EACH ROW EXECUTE FUNCTION public.check_package_card_limit();


--
-- Name: deck max_cards_per_user; Type: TRIGGER; Schema: public; Owner: josip
--

CREATE TRIGGER max_cards_per_user BEFORE INSERT OR UPDATE ON public.deck FOR EACH ROW EXECUTE FUNCTION public.check_max_cards_per_user();


--
-- Name: deck fk_cardid; Type: FK CONSTRAINT; Schema: public; Owner: josip
--

ALTER TABLE ONLY public.deck
    ADD CONSTRAINT fk_cardid FOREIGN KEY (fk_cardid) REFERENCES public.card(id) ON DELETE CASCADE;


--
-- Name: card fk_ownerid; Type: FK CONSTRAINT; Schema: public; Owner: josip
--

ALTER TABLE ONLY public.card
    ADD CONSTRAINT fk_ownerid FOREIGN KEY (fk_ownerid) REFERENCES public."user"(id) ON DELETE CASCADE;


--
-- Name: card fk_packid; Type: FK CONSTRAINT; Schema: public; Owner: josip
--

ALTER TABLE ONLY public.card
    ADD CONSTRAINT fk_packid FOREIGN KEY (fk_packid) REFERENCES public.package(id) ON DELETE CASCADE;


--
-- Name: battle fk_player1id; Type: FK CONSTRAINT; Schema: public; Owner: josip
--

ALTER TABLE ONLY public.battle
    ADD CONSTRAINT fk_player1id FOREIGN KEY (fk_player1id) REFERENCES public."user"(id) ON DELETE CASCADE;


--
-- Name: trade fk_player1id; Type: FK CONSTRAINT; Schema: public; Owner: josip
--

ALTER TABLE ONLY public.trade
    ADD CONSTRAINT fk_player1id FOREIGN KEY (fk_user1id) REFERENCES public."user"(id) ON DELETE CASCADE;


--
-- Name: battle fk_player2id; Type: FK CONSTRAINT; Schema: public; Owner: josip
--

ALTER TABLE ONLY public.battle
    ADD CONSTRAINT fk_player2id FOREIGN KEY (fk_player2id) REFERENCES public."user"(id) ON DELETE CASCADE;


--
-- Name: trade fk_player2id; Type: FK CONSTRAINT; Schema: public; Owner: josip
--

ALTER TABLE ONLY public.trade
    ADD CONSTRAINT fk_player2id FOREIGN KEY (fk_user2id) REFERENCES public."user"(id) ON DELETE CASCADE;


--
-- Name: package fk_userid; Type: FK CONSTRAINT; Schema: public; Owner: josip
--

ALTER TABLE ONLY public.package
    ADD CONSTRAINT fk_userid FOREIGN KEY (fk_userid) REFERENCES public."user"(id) ON DELETE CASCADE;


--
-- Name: deck fk_userid; Type: FK CONSTRAINT; Schema: public; Owner: josip
--

ALTER TABLE ONLY public.deck
    ADD CONSTRAINT fk_userid FOREIGN KEY (fk_userid) REFERENCES public."user"(id) ON DELETE CASCADE;


--
-- Name: stat fk_userid; Type: FK CONSTRAINT; Schema: public; Owner: josip
--

ALTER TABLE ONLY public.stat
    ADD CONSTRAINT fk_userid FOREIGN KEY (fk_userid) REFERENCES public."user"(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

