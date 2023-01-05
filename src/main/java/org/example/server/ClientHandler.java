package org.example.server;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.App;
import org.example.http.ContentType;
import org.example.http.HttpStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class ClientHandler implements Runnable {
    private Request request;
    private Response response;
    private Socket clientSocket;
    private PrintWriter outputStream;
    private BufferedReader inputStream;
    private App app;

    public ClientHandler(App app, Socket clientSocket) {
        this.app = app;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            setInputStream(new BufferedReader(new InputStreamReader(clientSocket.getInputStream())));
            setRequest(new Request(getInputStream()));
            setOutputStream(new PrintWriter(clientSocket.getOutputStream(), true));

            if (request.getPathname() == null) {
                setResponse(new Response(
                        HttpStatus.BAD_REQUEST,
                        ContentType.TEXT,
                        ""
                ));
            } else {
                setResponse(getApp().handleRequest(request));
            }

            getOutputStream().write(getResponse().build());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (getOutputStream() != null) {
                    getOutputStream().close();
                }
                if (getInputStream() != null) {
                    getInputStream().close();
                    getClientSocket().close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
