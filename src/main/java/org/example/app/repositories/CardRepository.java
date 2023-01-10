package org.example.app.repositories;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.models.*;
import org.example.app.models.Package;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Setter
@Getter
public class CardRepository implements Repository {
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    ConnectionPool connectionPool;

    public CardRepository(ConnectionPool connectionPool) {
        setConnectionPool(connectionPool);
    }


    private PreparedStatement createInsertPackageStatement(Package pack, Connection connection) throws SQLException {
        String sql = "INSERT INTO package(id, price, fk_userid)\n" +
                "VALUES(?, ?, ?);";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, pack.getId());
        ps.setInt(2, pack.getPrice());
        String id = pack.getUser() == null ? null : pack.getUser().getId();
        ps.setString(3, id);
        return ps;
    }

    private PreparedStatement createInsertCardsStatement(Card card, Connection connection) throws SQLException {
        String sql = "INSERT INTO card(id, name, damage, elementtype, cardtype, fk_ownerid, fk_packid) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?);";
        PreparedStatement ps = connection.prepareStatement(sql);

        ps.setString(1, card.getId());
        ps.setString(2, card.getName());
        ps.setDouble(3, card.getDamage());
        ps.setString(4, card.getElementType().toString());
        ps.setString(5, card.getCardType().toString());
        String id = card.getOwner() == null ? null : card.getOwner().getId();
        ps.setString(6, id);
        ps.setString(7, card.getPack().getId());
        return ps;
    }


    public void insert(Package pack) {
        getConnectionPool().executeAtomicTransaction((connection) -> {
            try (
                    PreparedStatement ps = createInsertPackageStatement(pack, connection)
            ) {
                ps.execute();
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }

            for (Card card : pack.getCards()) {
                try (
                        PreparedStatement psCards = createInsertCardsStatement(card, connection)
                ) {
                    psCards.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private PreparedStatement createUpdatePackageStatement(Package pack, Connection connection) throws SQLException {
        String sql = "UPDATE package SET fk_userid=? WHERE id=?;";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, pack.getUser().getId());
        ps.setString(2, pack.getId());
        return ps;
    }

    private PreparedStatement createUpdateCardStatement(Card card, Connection connection) throws SQLException {
        String sql = "UPDATE card SET fk_ownerid=? WHERE id=?;";
        PreparedStatement ps = connection.prepareStatement(sql);

        ps.setString(1, card.getOwner().getId());
        ps.setString(2, card.getId());
        return ps;
    }

    public void updatePack(Package pack, Connection connection) {
        try (
                PreparedStatement ps = createUpdatePackageStatement(pack, connection)
        ) {
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        for (Card card : pack.getCards()) {
            try (
                    PreparedStatement psCards = createUpdateCardStatement(card, connection)
            ) {
                psCards.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    private PreparedStatement createUpdateCoinsStatement(User authenticatedUser, Connection connection) throws SQLException {
        String sql = "UPDATE \"user\" SET coins=? WHERE id=?;";
        PreparedStatement ps = connection.prepareStatement(sql);

        ps.setInt(1, authenticatedUser.getCoins());
        ps.setString(2, authenticatedUser.getId());
        return ps;
    }

    public void update(Package pack, User authenticatedUser) {
        getConnectionPool().executeAtomicTransaction((connection) -> {
            updatePack(pack, connection);

            try (
                    PreparedStatement ps = createUpdateCoinsStatement(authenticatedUser, connection)
            ) {
                ps.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private PreparedStatement createDeleteDeckStatement(Deck deck, Connection connection) throws SQLException {
        String sql = "DELETE FROM deck WHERE fk_userid = ?";
        PreparedStatement ps = connection.prepareStatement(sql);

        ps.setString(1, deck.getUser().getId());
        return ps;
    }

    private PreparedStatement createInsertDeckStatement(Deck deck, Card card, Connection connection) throws SQLException {
        String sql = "INSERT INTO deck(fk_userid, fk_cardid) " +
                "VALUES (?,?);";
        PreparedStatement ps = connection.prepareStatement(sql);

        ps.setString(1, deck.getUser().getId());
        ps.setString(2, card.getId());
        return ps;
    }

    public void updateDeck(Deck deck) {
        getConnectionPool().executeAtomicTransaction((connection) -> {
            try (
                    PreparedStatement psDelete = createDeleteDeckStatement(deck, connection)
            ) {
                psDelete.execute();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            for (Card card : deck.getCards()) {
                try (
                        PreparedStatement ps = createInsertDeckStatement(deck, card, connection)
                ) {
                    ps.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public PreparedStatement createSelectCardStatement(Connection connection) throws SQLException {
        String sql = "SELECT id, name, damage, elementtype, cardtype, fk_ownerid, fk_packid FROM card;";
        return connection.prepareStatement(sql);
    }

    public PreparedStatement createSelectPackStatement(Connection connection) throws SQLException {
        String sql = "SELECT id, price, fk_userid FROM package";
        return connection.prepareStatement(sql);
    }

    public PreparedStatement createSelectDeckStatement(Connection connection) throws SQLException {
        String sql = "SELECT fk_userid, fk_cardid  FROM deck";
        return connection.prepareStatement(sql);
    }

    public List<Package> loadAll(List<User> users) {
        List<Card> cards = new ArrayList<>();
        List<Package> packs = new ArrayList<>();

        getConnectionPool().executeQuery(connection -> {
            try (
                    PreparedStatement cardStatement = createSelectCardStatement(connection);
                    PreparedStatement packStatement = createSelectPackStatement(connection);
                    PreparedStatement deckStatement = createSelectDeckStatement(connection);
                    ResultSet rsCards = cardStatement.executeQuery();
                    ResultSet rsPacks = packStatement.executeQuery();
                    ResultSet rsDecks = deckStatement.executeQuery()
            ) {
                while (rsPacks.next()) {
                    String id = rsPacks.getString(1);
                    int price = rsPacks.getInt(2);
                    String fkUserid = rsPacks.getString(3);
                    User user = users.stream()
                            .filter(u -> Objects.equals(u.getId(), fkUserid))
                            .findFirst().orElse(null);


                    Package pack = new Package(id, price, user);
                    packs.add(pack);
                }

                while (rsCards.next()) {
                    String id = rsCards.getString(1);
                    String name = rsCards.getString(2);
                    double damage = rsCards.getDouble(3);
                    String elementType = rsCards.getString(4);
                    String cardType = rsCards.getString(5);
                    String fkOwnerId = rsCards.getString(6);
                    String fkPackId = rsCards.getString(7);


                    User owner = users.stream()
                            .filter(u -> Objects.equals(u.getId(), fkOwnerId))
                            .findFirst().orElse(null);
                    Package pack = packs.stream().
                            filter(p -> Objects.equals(p.getId(), fkPackId))
                            .findFirst().orElse(null);

                    Card card = new Card(id, name, damage, elementType, cardType, owner, pack);

                    cards.add(card);
                }

                for (Package pack : packs) {
                    pack.setCards(cards.stream().filter(card -> Objects.equals(card.getPack().getId(), pack.getId())).toList());
                }



                while (rsDecks.next()) {
                    String fkUserId = rsDecks.getString(1);
                    String fkCardId = rsDecks.getString(2);

                    User user = users.stream()
                            .filter(u -> Objects.equals(u.getId(), fkUserId))
                            .findFirst().orElse(null);


                    Card card = cards.stream().
                            filter(c -> Objects.equals(c.getId(), fkCardId))
                            .findFirst().orElse(null);

                    if (user != null) {
                        user.getDeck().getCards().add(card);
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        return packs;
    }
}
