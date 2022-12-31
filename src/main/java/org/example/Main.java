package org.example;

import org.example.app.App;
import org.example.server.Server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        App app = new App();
        Server server = new Server(app, 80);
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
