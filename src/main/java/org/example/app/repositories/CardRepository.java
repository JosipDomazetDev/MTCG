package org.example.app.repositories;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.models.Card;
import org.example.app.models.Deck;
import org.example.app.models.Package;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Setter
@Getter
public class CardRepository implements Repository<Package> {
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    Connection connection;

    public CardRepository(Connection connection) {
        setConnection(connection);
    }


    private PreparedStatement createInsertPackageStatement(Package pack) throws SQLException {
        String sql = "INSERT INTO package(id, price, fk_userid)\n" +
                "VALUES(?, ?, ?);";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, pack.getId());
        ps.setInt(2, pack.getPrice());
        String id = pack.getUser() == null ? null : pack.getUser().getId();
        ps.setString(3, id);
        return ps;
    }

    private PreparedStatement createInsertCardsStatement(Card card) throws SQLException {
        String sql = "INSERT INTO card(id, name, damage, elementtype, cardtype, fk_ownerid, fk_packid) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?);";
        PreparedStatement ps = connection.prepareStatement(sql);

        ps.setString(1, card.getId());
        ps.setString(2, card.getName());
        ps.setDouble(3, card.getDamage());
        ps.setString(4, card.getElementType().toString().toLowerCase());
        ps.setString(5, card.getCardType().toString().toLowerCase());
        String id = card.getOwner() == null ? null : card.getOwner().getId();
        ps.setString(6, id);
        ps.setString(7, card.getPack().getId());
        return ps;
    }


    @Override
    public void insert(Package pack) {
        try (
                PreparedStatement ps = createInsertPackageStatement(pack);
        ) {
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        for (Card card : pack.getCards()) {
            try (
                    PreparedStatement psCards = createInsertCardsStatement(card)
            ) {
                psCards.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    private PreparedStatement createUpdatePackageStatement(Package pack) throws SQLException {
        String sql = "UPDATE package SET fk_userid=? WHERE id=?;";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, pack.getUser().getId());
        ps.setString(2, pack.getId());
        return ps;
    }

    private PreparedStatement createUpdateCardStatement(Card card) throws SQLException {
        String sql = "UPDATE card SET fk_ownerid=? WHERE id=?;";
        PreparedStatement ps = connection.prepareStatement(sql);

        ps.setString(1, card.getOwner().getId());
        ps.setString(2, card.getId());
        return ps;
    }


    @Override
    public void update(Package pack) {
        try (
                PreparedStatement ps = createUpdatePackageStatement(pack);
        ) {
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        for (Card card : pack.getCards()) {
            try (
                    PreparedStatement psCards = createUpdateCardStatement(card)
            ) {
                psCards.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private PreparedStatement createDeleteDeckStatement(Deck deck) throws SQLException {
        String sql = "DELETE FROM deck WHERE fk_userid = ?";
        PreparedStatement ps = connection.prepareStatement(sql);

        ps.setString(1, deck.getUser().getId());
        return ps;
    }

    private PreparedStatement createInsertDeckStatement(Deck deck, Card card) throws SQLException {
        String sql = "INSERT INTO deck(fk_userid, fk_cardid) " +
                "VALUES (?,?);";
        PreparedStatement ps = connection.prepareStatement(sql);

        ps.setString(1, deck.getUser().getId());
        ps.setString(2, card.getId());
        return ps;
    }

    public void updateDeck(Deck deck) {

        try (
                PreparedStatement psDelete = createDeleteDeckStatement(deck);
        ) {
            psDelete.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (Card card : deck.getCards()) {
            try (
                    PreparedStatement ps = createInsertDeckStatement(deck, card);
            ) {
                ps.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }


}
