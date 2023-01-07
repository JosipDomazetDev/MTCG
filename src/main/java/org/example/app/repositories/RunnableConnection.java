package org.example.app.repositories;

import java.sql.Connection;

@FunctionalInterface
public interface RunnableConnection {
    void run(Connection connection);
}

