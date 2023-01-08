package org.example.app.repositories;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.models.Stat;
import org.example.app.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Setter
@Getter
public class UserRepository implements Repository {
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    ConnectionPool connectionPool;

    public UserRepository(ConnectionPool connectionPool) {
        setConnectionPool(connectionPool);
    }


    private PreparedStatement createInsertUserStatement(User user, Connection connection) throws SQLException {
        String sql = "INSERT INTO \"user\"(id, username, passwordHash, coins, name, bio, image)\n" +
                "VALUES(?, ?, ?, ?, ?, ?, ?);";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, user.getId());
        ps.setString(2, user.getUsername());
        ps.setString(3, user.getPasswordHash());
        ps.setInt(4, user.getCoins());
        ps.setString(5, user.getName());
        ps.setString(6, user.getBio());
        ps.setString(7, user.getImage());
        return ps;
    }

    private PreparedStatement createInsertStatStatement(User user, Connection connection) throws SQLException {
        String sql = "INSERT INTO stat(fk_userid) " +
                "VALUES(?);";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, user.getId());
        return ps;
    }



    public void insert(User user) {
        getConnectionPool().executeAtomicTransaction((connection) -> {
            try (
                    PreparedStatement psUser = createInsertUserStatement(user, connection);
                    PreparedStatement psStat = createInsertStatStatement(user, connection)
            ) {
                psUser.execute();
                psStat.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private PreparedStatement createUpdateStatement(User user, Connection connection) throws SQLException {
        String sql = "UPDATE \"user\" SET name=?, bio=?, image=? WHERE id = ?";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, user.getName());
        ps.setString(2, user.getBio());
        ps.setString(3, user.getImage());
        ps.setString(4, user.getId());
        return ps;
    }

    public void update(User user) {
        getConnectionPool().executeAtomicTransaction((connection) -> {
            try (
                    PreparedStatement ps = createUpdateStatement(user, connection)
            ) {
                ps.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public List<User> loadAll() {
        List<User> users = new ArrayList<>();
        List<Stat> stats = new ArrayList<>();

        getConnectionPool().executeQuery(connection -> {
            try (
                    PreparedStatement ps = createSelectUserStatement(connection);
                    PreparedStatement psStats = createSelectStatStatement(connection);
                    ResultSet rsUsers = ps.executeQuery();
                    ResultSet rsStats = psStats.executeQuery()
            ) {
                while (rsStats.next()) {
                    String fkUserId = rsStats.getString(1);
                    int elo = rsStats.getInt(2);
                    int wins = rsStats.getInt(3);
                    int draws = rsStats.getInt(4);
                    int total = rsStats.getInt(5);
                    Stat stat = new Stat(fkUserId, elo, wins, draws, total);
                    stats.add(stat);
                }

                while (rsUsers.next()) {
                    String id = rsUsers.getString(1);
                    String passwordHash = rsUsers.getString(2);
                    int coins = rsUsers.getInt(3);
                    String username = rsUsers.getString(4);
                    String name = rsUsers.getString(5);
                    String bio = rsUsers.getString(6);
                    String image = rsUsers.getString(7);

                    Stat stat = stats.stream().filter(stat1 -> Objects.equals(stat1.getFkUserId(), id)).findFirst().orElse(null);

                    User user = new User(id, passwordHash, coins, username, name, bio, image, stat);

                    users.add(user);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        });


        return users;
    }

    private PreparedStatement createSelectUserStatement(Connection connection) throws SQLException {
        String sql = "SELECT id,passwordhash,coins,username,name,bio,image FROM \"user\";";
        return connection.prepareStatement(sql);
    }

    private PreparedStatement createSelectStatStatement(Connection connection) throws SQLException {
        String sql = "SELECT fk_userid, elo, wins, draws, total FROM stat";
        return connection.prepareStatement(sql);
    }

}
