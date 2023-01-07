package org.example.server;

import java.sql.SQLException;

public interface ServerApp {
    Response handleRequest(Request request) throws SQLException;
}
