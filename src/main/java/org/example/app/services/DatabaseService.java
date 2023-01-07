package org.example.app.services;

import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Getter
@Setter
public class DatabaseService {
    // The service used to communicate to the database
    private Connection connection;

    public DatabaseService() throws SQLException {
        setConnection(createNewConnection());
    }

    public static Connection createNewConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:postgresql://localhost:5000/mtcg",
                "josip",
                "josip"
        );
    }

    static public void executeTransaction(Connection connection, Runnable action) {
        try {
            connection.setAutoCommit(false);
            try {
                action.run();
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
