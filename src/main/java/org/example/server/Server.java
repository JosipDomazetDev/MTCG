package org.example.server;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.App;
import org.example.app.services.ConnectionPool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class Server {
    private Request request;
    private Response response;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter outputStream;
    private BufferedReader inputStream;
    private App app;
    private int port;

    public Server(App app, int port) {
        setApp(app);
        setPort(port);
    }

    public void start() throws IOException {
        setServerSocket(new ServerSocket(getPort()));

        run();
    }

    private void run() {
        while (true) {
            try {
                setClientSocket(getServerSocket().accept());

                Thread thread = new Thread(new ClientHandler(app, clientSocket));
                thread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}