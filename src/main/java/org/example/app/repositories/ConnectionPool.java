package org.example.app.repositories;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionPool {
    private static final String JDBC_URL = "jdbc:postgresql://localhost:5000/mtcg";
    private static final String USERNAME = "josip";
    private static final String PASSWORD = "josip";

    private static final int INIT_POOL_SIZE = 10;
    private static final int MAX_POOL_SIZE = 20;

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

    public synchronized Connection getConnection() {
        Connection connection;
        try {
            connection = connections.take();
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

        } catch (InterruptedException e) {
            // Returning any connection is the best we can do here
            return connections.poll();
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


    public void executeAtomicTransaction(RunnableConnection action) {
        Connection connection = getConnection();
        try {
            if (connection.isClosed()) {
                System.err.println("Connection was closed before transaction could be executed.");
                releaseConnection(connection);
                return;
            }

            connection.setAutoCommit(false);
            try {
                action.run(connection);
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

        releaseConnection(connection);
    }

    public void executeQuery(RunnableConnection action) {
        Connection connection = getConnection();
        try {
            if (connection.isClosed()) {
                System.err.println("Connection was closed before transaction could be executed.");
                releaseConnection(connection);
                return;
            }
            action.run(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        releaseConnection(connection);
    }
}
