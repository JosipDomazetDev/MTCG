package org.example.app;

import org.example.app.controllers.*;
import org.example.app.models.User;
import org.example.app.services.CardService;
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

    @Setter(AccessLevel.PRIVATE)
    private SessionController sessionController;

    @Setter(AccessLevel.PRIVATE)
    private PackageController packageController;

    @Setter(AccessLevel.PRIVATE)
    private ErrorController errorController;

    public App() {
        setCityController(new CityController(new CityService()));

        UserService userService = new UserService();
        setUserController(new UserController(userService));
        setSessionController(new SessionController(userService));

        setPackageController(new PackageController(new CardService()));
        setErrorController(new ErrorController());
    }

    public Response handleRequest(Request request) {
        User authenticatedUser = sessionController.getAuthenticatedUser(request.getToken());
        boolean isAuthenticated = authenticatedUser != null;

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
                } else if (request.getPathname().equals("/sessions")) {
                    return this.sessionController.login(request);
                }

                // ============================ Authenticated Paths ============================

                if (!isAuthenticated) {
                    return this.errorController.sendUnauthorized(request);
                }

                if (request.getPathname().equals("/packages")) {
                    if (!authenticatedUser.isAdmin()){
                        return this.errorController.sendUnauthorized(request);
                    }

                    return this.packageController.createPackage(request, authenticatedUser);
                }
            }
        }

        return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{ \"error\": \"Not Found\", \"data\": null }");
    }
}
