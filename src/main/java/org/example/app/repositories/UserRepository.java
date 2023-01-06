package org.example.app.repositories;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.models.User;

import java.sql.*;

@Setter
@Getter
public class UserRepository implements Repository<User> {
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    Connection connection;


    public UserRepository(Connection connection) {
        setConnection(connection);
    }


    private PreparedStatement createInsertStatement(User user) throws SQLException {
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

    private PreparedStatement createInsertStatStatement(User user) throws SQLException {
        String sql = "INSERT INTO stat(fk_userid) " +
                "VALUES(?);";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, user.getId());
        return ps;
    }


    @Override
    public void insert(User user) {
        try (
                PreparedStatement ps = createInsertStatement(user);
                PreparedStatement psStat = createInsertStatStatement(user)
        ) {
            if (ps.execute()) {
                psStat.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private PreparedStatement createUpdateStatement(User user) throws SQLException {
        String sql = "UPDATE \"user\" SET name=?, bio=?, image=? WHERE id = ?";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, user.getName());
        ps.setString(2, user.getBio());
        ps.setString(3, user.getImage());
        ps.setString(4, user.getId());
        return ps;
    }

    @Override
    public void update(User user) {
        try (
                PreparedStatement ps = createUpdateStatement(user)
        ) {
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//    public List<User> getUser(int userId) {
//        try (
//             PreparedStatement ps = createPreparedStatement( userId);
//             ResultSet rs = ps.executeQuery()) {
//
//            // process the resultset here, all resources will be cleaned up
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private PreparedStatement createPreparedStatement( int userId) throws SQLException {
//        String sql = "SELECT id, username FROM users WHERE id = ?";
//        PreparedStatement ps = connection.prepareStatement(sql);
//        ps.setInt(1, userId);
//        return ps;
//    }


}
