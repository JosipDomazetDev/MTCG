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
        setConnection(
            DriverManager.getConnection(
                "jdbc:postgresql://localhost:5000/mtcg",
                "josip",
                "josip"
            )
        );
    }
}
