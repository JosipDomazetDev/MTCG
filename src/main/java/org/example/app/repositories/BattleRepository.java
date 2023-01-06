package org.example.app.repositories;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.models.Battle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Setter
@Getter
public class BattleRepository implements Repository<Battle> {
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    Connection connection;
    private String id;

    public BattleRepository(Connection connection) {
        setConnection(connection);
    }


    private PreparedStatement createInsertBattleStatement(Battle battle) throws SQLException {
        String sql = "INSERT INTO battle(id, fk_player1id, fk_player2id, battlelog, battleoutcome) VALUES " +
                "(?,?,?,?,?);";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, battle.getId());
        ps.setString(2, battle.getPlayer1().getId());
        ps.setString(3, battle.getPlayer2().getId());
        ps.setString(4, battle.getBattleLog().toString());
        ps.setString(5, battle.getBattleOutcome().toString());

        return ps;
    }

    @Override
    public void insert(Battle battle) {
        try (
                PreparedStatement ps = createInsertBattleStatement(battle);
        ) {
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Battle battle) {
    }

}
