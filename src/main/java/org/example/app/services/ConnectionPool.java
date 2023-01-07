package org.example.app.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionPool {
    private static final String JDBC_URL = "jdbc:postgresql://localhost:5000/mtcg";
    private static final String USERNAME = "josip";
    private static final String PASSWORD = "josip";

    private static final int INIT_POOL_SIZE = 1;
    private static final int MAX_POOL_SIZE = 1;

    private final BlockingQueue<Connection> connections = new LinkedBlockingQueue<>(MAX_POOL_SIZE);

    public ConnectionPool() {
        initPool();
    }

    private void initPool() {
        try {
            Class.forName("org.postgresql.Driver");
            for (int i = 0; i < INIT_POOL_SIZE; i++) {
                Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
                connections.offer(connection);
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized Connection getConnection() throws InterruptedException {
        Connection connection = connections.take();
        int counter = 0;
        int MAX_TRIES = 10;

        try {
            while (connection.isClosed()) {
                // If the connection is closed, remove it from the queue and create a new connection
                connections.remove(connection);
                connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);

                // Add the new connection back to the queue
                connections.offer(connection);
                connection = connections.take();

                counter++;
                if (counter >= MAX_TRIES) {
                    System.err.println("Cannot create new connection.");
                    break;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return connection;
    }

    public synchronized void releaseConnection(Connection connection) {
        try {
            if (!connection.isClosed()) {
                // If the connection is active, add it back to the queue
                if (connections.size() < MAX_POOL_SIZE) {
                    connections.offer(connection);
                } else {
                    // Close it if there are too many active connections.
                    connection.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
