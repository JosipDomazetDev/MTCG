-- docker exec cb0e090df0ca createdb -U josip mtcg

drop table if exists "user" CASCADE;
drop table if exists "package" CASCADE;
drop table if exists "card" CASCADE;
drop table if exists "deck" CASCADE;
drop table if exists "stat" CASCADE;
drop table if exists "trade" CASCADE;
drop table if exists "battle" CASCADE;

create table if not exists "user"
(
    id           varchar           not null PRIMARY KEY,
    passwordHash varchar           not null,
    coins        integer,
    username     varchar COLLATE "C" not null,
    name         varchar COLLATE "C",
    bio          varchar COLLATE "C",
    image        varchar COLLATE "C"
);

create unique index if not exists user_username_uindex
    on "user" (username);

create table if not exists package
(
    id        varchar PRIMARY KEY,
    price     integer,
    fk_userId varchar,
    CONSTRAINT fk_userId
        FOREIGN KEY (fk_userId)
            REFERENCES "user" (id)
            ON DELETE CASCADE
);


create table if not exists card
(
    id          varchar PRIMARY KEY,
    name        varchar COLLATE "C" not null,
    damage      double precision not null,
    elementType varchar,
    cardType    varchar,
    fk_ownerId  varchar,
    fk_packId   varchar,
    CONSTRAINT fk_ownerId
        FOREIGN KEY (fk_ownerId)
            REFERENCES "user" (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_packId
        FOREIGN KEY (fk_packId)
            REFERENCES "package" (id)
            ON DELETE CASCADE
);



create table if not exists deck
(
    fk_userId varchar,
    fk_cardId varchar,
    PRIMARY KEY (fk_cardId, fk_userId),
    CONSTRAINT fk_userId
        FOREIGN KEY (fk_userId)
            REFERENCES "user" (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_cardId
        FOREIGN KEY (fk_cardId)
            REFERENCES "card" (id)
            ON DELETE CASCADE
);


CREATE OR REPLACE FUNCTION check_max_cards_per_user()
    RETURNS TRIGGER AS
$$
BEGIN
    IF (SELECT COUNT(*) FROM deck WHERE fk_userId = NEW.fk_userId) > 4 THEN
        RAISE EXCEPTION 'Cannot have more than 4 cards per user';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER max_cards_per_user
    BEFORE INSERT OR UPDATE
    ON deck
    FOR EACH ROW
EXECUTE PROCEDURE check_max_cards_per_user();


create table if not exists stat
(
    fk_userId varchar PRIMARY KEY,
    elo       integer,
    wins      integer,
    draws     integer,
    total     integer,
    CONSTRAINT fk_userId
        FOREIGN KEY (fk_userId)
            REFERENCES "user" (id)
            ON DELETE CASCADE
);

create table if not exists battle
(
    id            varchar PRIMARY KEY,
    fk_player1Id  varchar,
    fk_player2Id  varchar,
    battleLog     varchar COLLATE "C",
    battleOutcome varchar,
    created_at    TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_player1Id
        FOREIGN KEY (fk_player1Id)
            REFERENCES "user" (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_player2Id
        FOREIGN KEY (fk_player2Id)
            REFERENCES "user" (id)
            ON DELETE CASCADE
);


create table if not exists trade
(
    id               varchar PRIMARY KEY,
    fk_cardToTradeId varchar,
    cardType         varchar,
    minimumDamage    double precision,
    fk_user1Id       varchar,
    fk_user2Id       varchar,
    CONSTRAINT fk_player1Id
        FOREIGN KEY (fk_user1Id)
            REFERENCES "user" (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_player2Id
        FOREIGN KEY (fk_user2Id)
            REFERENCES "user" (id)
            ON DELETE CASCADE
);
INSERT INTO "user"(id, username, passwordHash, coins, name, bio, image)
VALUES ('josip', 'josip', 'josip', 20, 'gkrkrg', 'drfrggr', 'fefrk');
