package org.example.app.repositories;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.models.Battle;
import org.example.app.models.Card;
import org.example.app.models.Trade;
import org.example.app.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Setter
@Getter
public class TradeRepository implements Repository<Trade> {
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    Connection connection;

    public TradeRepository(Connection connection) {
        setConnection(connection);
    }


    private PreparedStatement createInsertTradeStatement(Trade trade) throws SQLException {
        String sql = "INSERT INTO trade(id, fk_cardtotradeid, cardtype, minimumdamage, fk_user1id, fk_user2id) VALUES " +
                "(?,?,?,?,?,?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, trade.getId());
        ps.setString(2, trade.getCard().getId());
        ps.setString(3, trade.getCardType().toString());
        ps.setDouble(4, trade.getMinimumDamage());
        ps.setString(5, trade.getUser1().getId());

        String id2 = trade.getUser2() == null ? null : trade.getUser2().getId();
        ps.setString(6, id2);

        return ps;
    }

    @Override
    public void insert(Trade trade) {
        try (
                PreparedStatement ps = createInsertTradeStatement(trade);
        ) {
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Trade trade) {
    }


    private PreparedStatement createDeleteTradeStatement(String tradeId) throws SQLException {
        String sql = "DELETE FROM trade WHERE id=?;";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, tradeId);
        return ps;
    }

    public void delete(String tradeId) {
        try (
                PreparedStatement ps = createDeleteTradeStatement(tradeId);
        ) {
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private PreparedStatement createUpdateCardOwnerStatement(Card card) throws SQLException {
        String sql = "UPDATE card SET fk_ownerid=? WHERE id=?;";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, card.getOwner().getId());
        ps.setString(2, card.getId());

        return ps;
    }

    private PreparedStatement createUpdateTradeStatement(Trade trade) throws SQLException {
        String sql = "UPDATE trade SET fk_user2id=? WHERE id=?;";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, trade.getUser2().getId());
        ps.setString(2, trade.getId());

        return ps;
    }

    public void performTrade(List<Object> result) {
        Card card = (Card) result.get(0);
        Card offeredCard = (Card) result.get(1);
        Trade trade = (Trade) result.get(2);

        try (
                PreparedStatement ps1 = createUpdateCardOwnerStatement(card);
                PreparedStatement ps2 = createUpdateCardOwnerStatement(offeredCard);
                PreparedStatement psTrade = createUpdateTradeStatement(trade);
        ) {
            ps1.execute();
            ps2.execute();
            psTrade.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private PreparedStatement createSelectTradesStatement() throws SQLException {
        String sql = "SELECT id, fk_cardtotradeid, cardtype, minimumdamage, fk_user1id, fk_user2id FROM trade;";
        return connection.prepareStatement(sql);
    }

    public List<Trade> loadAll(List<User> users, List<Card> cards) {
        List<Trade> trades = new ArrayList<>();

        try (
                PreparedStatement selectTradesStatement = createSelectTradesStatement();
                ResultSet rsTrades = selectTradesStatement.executeQuery();
        ) {
            while (rsTrades.next()) {
                String id = rsTrades.getString(1);
                String fkCardToTradeId = rsTrades.getString(2);
                String cardType = rsTrades.getString(3);
                double minimumDamage = rsTrades.getDouble(4);
                String fkUser1id = rsTrades.getString(5);
                String fkUser2id = rsTrades.getString(6);


                User user1 = users.stream()
                        .filter(u -> Objects.equals(u.getId(), fkUser1id))
                        .findFirst().orElse(null);
                User user2 = users.stream()
                        .filter(u -> Objects.equals(u.getId(), fkUser2id))
                        .findFirst().orElse(null);
                Card card = cards.stream()
                        .filter(c -> Objects.equals(c.getId(), fkCardToTradeId))
                        .findFirst().orElse(null);

                Trade trade = null;
                if (card != null) {
                    trade = new Trade(id, card, cardType, minimumDamage, user1, user2);
                }

                trades.add(trade);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return trades;
    }
}
