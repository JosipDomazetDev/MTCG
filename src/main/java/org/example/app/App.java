package org.example.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.app.controllers.*;
import org.example.app.models.User;
import org.example.app.services.BattleService;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
    private CardController cardController;

    @Setter(AccessLevel.PRIVATE)
    private StatController statController;

    @Setter(AccessLevel.PRIVATE)
    private BattleController battleController;

    @Setter(AccessLevel.PRIVATE)
    private ErrorController errorController;

    public App() {
        setCityController(new CityController(new CityService()));

        UserService userService = new UserService();
        setUserController(new UserController(userService));
        setSessionController(new SessionController(userService));

        CardService cardService = new CardService();
        setPackageController(new PackageController(cardService));
        setCardController(new CardController(cardService));

        setStatController(new StatController(userService));

        setBattleController(new BattleController(new BattleService()));

        setErrorController(new ErrorController());
    }

    public Response handleRequest(Request request) {
        User authenticatedUser = sessionController.getAuthenticatedUser(request.getToken());
        boolean isAuthenticated = authenticatedUser != null;

        try {
            switch (request.getMethod()) {
                case GET: {
                    if (request.getPathname().equals("/cities")) {
                        return this.cityController.getCities();
                    }

                    if (!isAuthenticated) {
                        return this.errorController.sendUnauthorized(request);
                    }

                    String matchesUserPath = matchesUserPath(request);

                    if (matchesUserPath != null) {
                        return this.userController.getUser(matchesUserPath, authenticatedUser);
                    } else if (request.getPathname().equals("/cards")) {
                        return this.cardController.getCards(authenticatedUser);
                    } else if (request.getPathname().equals("/decks")) {
                        boolean plainMode = request.getParams().contains("format=plain");
                        return this.cardController.getCardsFromDeck(authenticatedUser, plainMode);
                    } else if (request.getPathname().equals("/stats")) {
                        return this.statController.getStats(authenticatedUser);
                    } else if (request.getPathname().equals("/scores")) {
                        return this.statController.getScores();
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
                        if (!authenticatedUser.isAdmin()) {
                            return this.errorController.sendUnauthorized(request);
                        }

                        return this.packageController.createPackage(request, authenticatedUser);
                    }

                    if (request.getPathname().equals("/transactions/packages")) {
                        return this.packageController.buyPackage(authenticatedUser);
                    }

                    if (request.getPathname().equals("/battles")) {
                        return this.battleController.createOrStartBattle(authenticatedUser);
                    }
                }
                case PUT: {
                    String matchesUserPath = matchesUserPath(request);

                    if (matchesUserPath != null) {
                        return this.userController.putUser(request, matchesUserPath, authenticatedUser);
                    } else if (request.getPathname().equals("/decks")) {
                        return this.cardController.putCardsIntoDeck(request, authenticatedUser);
                    }
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new Response(
                    HttpStatus.BAD_REQUEST,
                    ContentType.JSON,
                    "{ \"error\": \"Illegal JSON-Format!\", \"data\": null }"
            );
        }

        return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{ \"error\": \"Not Found\", \"data\": null }");
    }

    private static String matchesUserPath(Request request) {
        String pathRegex = "^/users/([\\w\\-\\.~:\\/\\?#\\[\\]@!$&'\\(\\)\\*\\+,;=]+)(?:\\?.*)?$";
        Pattern pathPattern = Pattern.compile(pathRegex);
        Matcher pathMatcher = pathPattern.matcher(request.getPathname());

        if (pathMatcher.matches()) {
            return pathMatcher.group(1);
        }

        return null;
    }
}
