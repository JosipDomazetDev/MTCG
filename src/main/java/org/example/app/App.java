package org.example.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.app.controllers.*;
import org.example.app.models.User;
import org.example.app.services.*;
import org.example.http.ContentType;
import org.example.http.HttpStatus;
import lombok.AccessLevel;
import lombok.Setter;
import org.example.server.Request;
import org.example.server.Response;
import org.example.server.ServerApp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.currentThread;


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
    private TradingController tradingController;

    @Setter(AccessLevel.PRIVATE)
    private ErrorController errorController;

    public App() {
        setCityController(new CityController(new CityService()));

        UserService userService = new UserService();
        setUserController(new UserController(userService));
        setSessionController(new SessionController(userService));

        CardService cardService = new CardService();
        TradingService tradingService = new TradingService();

        setPackageController(new PackageController(cardService));
        setCardController(new CardController(cardService, tradingService));

        setStatController(new StatController(userService));

        setBattleController(new BattleController(new BattleService()));

        setTradingController(new TradingController(tradingService, cardService));

        setErrorController(new ErrorController());
    }

    public Response handleRequest(Request request) {
        System.out.println("Request handled by Thread: " + currentThread().getName());

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

                    String matchesUserPath = matchesUserPath("users", request);

                    if (matchesUserPath != null) {
                        return this.userController.getUser(matchesUserPath, authenticatedUser);
                    }

                    if (request.getPathname().equals("/cards")) {
                        return this.cardController.getCards(authenticatedUser);
                    }
                    if (request.getPathname().equals("/decks")) {
                        boolean plainMode = request.getParams().contains("format=plain");
                        return this.cardController.getCardsFromDeck(authenticatedUser, plainMode);
                    }
                    if (request.getPathname().equals("/stats")) {
                        return this.statController.getStats(authenticatedUser);
                    }
                    if (request.getPathname().equals("/scores")) {
                        return this.statController.getScores();
                    }
                    if (request.getPathname().equals("/tradings")) {
                        boolean belongsToMe = request.getParams().contains("belongs=me");
                        return this.tradingController.getTrades(authenticatedUser, belongsToMe);
                    }
                }
                case POST: {
                    if (request.getPathname().equals("/users")) {
                        return this.userController.createUser(request);
                    }
                    if (request.getPathname().equals("/sessions")) {
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
                    if (request.getPathname().equals("/tradings")) {
                        return this.tradingController.postTrades(request, authenticatedUser);
                    }
                }
                case PUT: {
                    String matchesUserPath = matchesUserPath("users", request);

                    if (matchesUserPath != null) {
                        return this.userController.putUser(request, matchesUserPath, authenticatedUser);
                    }
                    if (request.getPathname().equals("/decks")) {
                        return this.cardController.putCardsIntoDeck(request, authenticatedUser);
                    }

                    String matchesTradingsPath = matchesUserPath("tradings", request);

                    if (matchesTradingsPath != null) {
                        return this.tradingController.performTrade(request, authenticatedUser, matchesTradingsPath);
                    }
                }
                case DELETE:
                    String matchesTradingsPath = matchesUserPath("tradings", request);

                    if (matchesTradingsPath != null) {
                        return this.tradingController.deleteTrade(authenticatedUser, matchesTradingsPath);
                    }
                    break;
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"error\": \"Illegal JSON-Format!\", \"data\": null }");
        }

        return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{ \"error\": \"Not Found\", \"data\": null }");
    }

    private static String matchesUserPath(String rootPath, Request request) {
        String pathRegex = "^/" + rootPath + "/([\\w\\-\\.~:\\/\\?#\\[\\]@!$&'\\(\\)\\*\\+,;=]+)(?:\\?.*)?$";
        Pattern pathPattern = Pattern.compile(pathRegex);
        Matcher pathMatcher = pathPattern.matcher(request.getPathname());

        if (pathMatcher.matches()) {
            return pathMatcher.group(1);
        }

        return null;
    }
}
