package org.example.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.app.controllers.*;
import org.example.app.models.Card;
import org.example.app.models.User;
import org.example.app.repositories.BattleRepository;
import org.example.app.repositories.CardRepository;
import org.example.app.repositories.TradeRepository;
import org.example.app.repositories.UserRepository;
import org.example.app.services.*;
import org.example.http.ContentType;
import org.example.http.HttpStatus;
import lombok.AccessLevel;
import lombok.Setter;
import org.example.server.Request;
import org.example.server.Response;
import org.example.server.ServerApp;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.currentThread;


public class App implements ServerApp {
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

    private static final ConnectionPool pool = new ConnectionPool();
    public App() {
        try {
            UserRepository userRepository = new UserRepository(pool);
            CardRepository cardRepository = new CardRepository(pool);
            BattleRepository battleRepository = new BattleRepository(pool);
            TradeRepository tradeRepository = new TradeRepository(pool);


            UserService userService = new UserService();
            setUserController(new UserController(userService, userRepository));
            setSessionController(new SessionController(userService));

            CardService cardService = new CardService();
            TradingService tradingService = new TradingService();

            setPackageController(new PackageController(cardService, cardRepository));
            setCardController(new CardController(cardService, tradingService, cardRepository));

            setStatController(new StatController(userService));

            setBattleController(new BattleController(new BattleService(), battleRepository));

            setTradingController(new TradingController(tradingService, cardService, tradeRepository));


            userController.loadAll();
            cardController.loadAll(userService.getUsers());
            battleController.loadAll(userService.getUsers());

            List<Card> cards = cardService.getPackages().stream().flatMap(aPackage -> aPackage.getCards().stream()).toList();
            tradingController.loadAll(userService.getUsers(), cards);

        } finally {

        }

    }

    public Response handleRequest(Request request) throws SQLException {
        System.out.println("Request handled by Thread: " + currentThread().getName());
        Response response = route(request);

        return response;
    }

    private Response route(Request request) {
        User authenticatedUser = sessionController.getAuthenticatedUser(request.getToken());
        boolean isAuthenticated = authenticatedUser != null;

        try {
            switch (request.getMethod()) {
                case GET -> {
                    if (!isAuthenticated) {
                        return ErrorController.sendUnauthorized(request);
                    }

                    String matchesUserPath = matchesRootPath("users", request);

                    if (matchesUserPath != null) {
                        return this.userController.getUser(matchesUserPath, authenticatedUser);
                    }

                    // Bonus-Feature
                    String matchesCardPath = matchesRootPath("cards", request);

                    if (matchesCardPath != null) {
                        return this.cardController.getCard(authenticatedUser, matchesCardPath);
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
                case POST -> {
                    if (request.getPathname().equals("/users")) {
                        return this.userController.createUser(request);
                    }
                    if (request.getPathname().equals("/sessions")) {
                        return this.sessionController.login(request);
                    }

                    if (!isAuthenticated) {
                        return ErrorController.sendUnauthorized(request);
                    }
                    if (request.getPathname().equals("/packages")) {
                        return this.packageController.createPackage(request, authenticatedUser);
                    }
                    if (request.getPathname().equals("/transactions/packages")) {
                        return this.packageController.buyPackage(authenticatedUser);
                    }
                    if (request.getPathname().equals("/battles")) {

                        return this.battleController.createOrStartBattle(authenticatedUser);
                    }

                    String matchesTradingsPath = matchesRootPath("tradings", request);
                    if (matchesTradingsPath != null) {
                        return this.tradingController.performTrade(request, authenticatedUser, matchesTradingsPath);
                    }

                    if (request.getPathname().equals("/tradings")) {
                        return this.tradingController.postTrades(request, authenticatedUser);
                    }
                }
                case PUT -> {
                    if (!isAuthenticated) {
                        return ErrorController.sendUnauthorized(request);
                    }

                    String matchesUserPath = matchesRootPath("users", request);

                    if (matchesUserPath != null) {
                        return this.userController.putUser(request, matchesUserPath, authenticatedUser);
                    }
                    if (request.getPathname().equals("/decks")) {
                        return this.cardController.putCardsIntoDeck(request, authenticatedUser);
                    }
                }
                case DELETE -> {
                    if (!isAuthenticated) {
                        return ErrorController.sendUnauthorized(request);
                    }
                    String matchesTradingsPath = matchesRootPath("tradings", request);
                    if (matchesTradingsPath != null) {
                        return this.tradingController.deleteTrade(authenticatedUser, matchesTradingsPath);
                    }
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"error\": \"Illegal JSON-Format!\"}");
        } catch (InterruptedException e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"error\": \"Your thread was interrupted!\"}");
        }

        return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{ \"error\": \"Not Found\"");
    }

    private static String matchesRootPath(String rootPath, Request request) {
        String pathRegex = "^/" + rootPath + "/([\\w\\-\\.~:\\/\\?#\\[\\]@!$&'\\(\\)\\*\\+,;=]+)(?:\\?.*)?$";
        Pattern pathPattern = Pattern.compile(pathRegex);
        Matcher pathMatcher = pathPattern.matcher(request.getPathname());

        if (pathMatcher.matches()) {
            return pathMatcher.group(1);
        }

        return null;
    }
}
