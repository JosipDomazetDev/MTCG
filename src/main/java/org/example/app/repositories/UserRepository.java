package org.example.app.repositories;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.models.PasswordUtils;
import org.example.app.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public List<User> loadAll() {
        List<User> users = new ArrayList<>();
        try (
                PreparedStatement ps = createSelectStatement();
                ResultSet rs = ps.executeQuery()) {


            while (rs.next()) {
                String id = rs.getString(1);
                String passwordHash = rs.getString(2);
                int coins = rs.getInt(3);
                String username = rs.getString(4);
                String name = rs.getString(5);
                String bio = rs.getString(6);
                String image = rs.getString(7);

                User user = new User(id, passwordHash, coins, username, name, bio, image);
                users.add(user);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    private PreparedStatement createSelectStatement() throws SQLException {
        String sql = "SELECT id,passwordhash,coins,username,name,bio,image FROM \"user\";";
        return connection.prepareStatement(sql);
    }


}
