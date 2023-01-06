package org.example.app.repositories;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.models.Battle;
import org.example.app.models.Card;
import org.example.app.models.Package;
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
public class BattleRepository implements Repository<Battle> {
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    Connection connection;

    public BattleRepository(Connection connection) {
        setConnection(connection);
    }


    private PreparedStatement createInsertBattleStatement(Battle battle) throws SQLException {
        String sql = "INSERT INTO battle(id, fk_player1id, fk_player2id, battlelog, battleoutcome) VALUES " + "(?,?,?,?,?);";
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
        try (PreparedStatement ps = createInsertBattleStatement(battle);) {
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        try (PreparedStatement ps1 = createUpdateStatsStatement(battle.getPlayer1());
             PreparedStatement ps2 = createUpdateStatsStatement(battle.getPlayer2())

        ) {
            ps1.execute();
            ps2.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private PreparedStatement createUpdateStatsStatement(User user) throws SQLException {
        String sql = "UPDATE stat SET elo=?, wins=?,draws=?, total=? WHERE fk_userid=?;";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, user.getStat().getElo());
        ps.setInt(2, user.getStat().getWins());
        ps.setInt(3, user.getStat().getDraws());
        ps.setInt(4, user.getStat().getTotal());
        ps.setString(5, user.getId());

        return ps;
    }

    @Override
    public void update(Battle battle) {
    }

    private PreparedStatement createSelectBattleStatement() throws SQLException {
        String sql = "SELECT id, fk_player1id, fk_player2id, battlelog, battleoutcome, created_at  FROM battle";
        return connection.prepareStatement(sql);
    }

    public List<Battle> loadAll(List<User> users) {
        List<Battle> battles = new ArrayList<>();

        try (
                PreparedStatement battleStatement = createSelectBattleStatement();
                ResultSet rsBattles = battleStatement.executeQuery();
        ) {
            while (rsBattles.next()) {
                String id = rsBattles.getString(1);
                String fkPlayer1Id = rsBattles.getString(2);
                String fkPlayer2Id = rsBattles.getString(3);
                String battleLog = rsBattles.getString(4);
                String battleOutcome = rsBattles.getString(5);


                User player1 = users.stream()
                        .filter(u -> Objects.equals(u.getId(), fkPlayer1Id))
                        .findFirst().orElse(null);
                User player2 = users.stream()
                        .filter(u -> Objects.equals(u.getId(), fkPlayer2Id))
                        .findFirst().orElse(null);

                Battle battle = new Battle(id, player1, player2, battleLog, battleOutcome);
                battles.add(battle);
            }



        } catch (SQLException e) {
            e.printStackTrace();
        }

        return battles;

    }
}
