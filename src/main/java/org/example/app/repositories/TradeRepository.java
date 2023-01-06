package org.example.app.repositories;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.models.Trade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
        ps.setString(3, trade.getCardType().toString().toLowerCase());
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

}
