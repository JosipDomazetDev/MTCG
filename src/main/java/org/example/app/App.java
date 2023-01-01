package org.example.app;

import org.example.app.controllers.CityController;
import org.example.app.controllers.UserController;
import org.example.app.services.CityService;
import org.example.app.services.UserService;
import org.example.http.ContentType;
import org.example.http.HttpStatus;
import lombok.AccessLevel;
import lombok.Setter;
import org.example.server.Request;
import org.example.server.Response;
import org.example.server.ServerApp;


public class App implements ServerApp {
    @Setter(AccessLevel.PRIVATE)
    private CityController cityController;
    @Setter(AccessLevel.PRIVATE)
    private UserController userController;

    public App() {
        setCityController(new CityController(new CityService()));
        setUserController(new UserController(new UserService()));
    }

    public Response handleRequest(Request request) {


        switch (request.getMethod()) {
            case GET: {
                if (request.getPathname().equals("/cities")) {
                    return this.cityController.getCities();
                } else if (request.getPathname().equals("/users")) {
                    return this.userController.getUsers();
                }

            }
            case POST: {
                if (request.getPathname().equals("/users")) {
                    return this.userController.createUser(request);
                    //return this.userController.createUser(request.getBody());
                }

            }
        }

        return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{ \"error\": \"Not Found\", \"data\": null }");
    }
}
