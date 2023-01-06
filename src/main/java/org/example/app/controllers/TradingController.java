package org.example.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.models.Card;
import org.example.app.models.Trade;
import org.example.app.models.User;
import org.example.app.repositories.TradeRepository;
import org.example.app.services.CardService;
import org.example.app.services.TradingService;
import org.example.app.services.exceptions.ConflictException;
import org.example.app.services.exceptions.NotAvailableException;
import org.example.http.ContentType;
import org.example.http.HttpStatus;
import org.example.server.Request;
import org.example.server.Response;

import java.util.List;

public class TradingController extends Controller {
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private TradingService tradingService;

    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private CardService cardService;

    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private TradeRepository tradeRepository;

    public TradingController(TradingService tradingService, CardService cardService, TradeRepository tradeRepository) {
        setTradingService(tradingService);
        setCardService(cardService);
        setTradeRepository(tradeRepository);
    }

    public Response getTrades(User authenticatedUser, boolean belongsToMe) throws JsonProcessingException {
        List<Trade> trades = tradingService.getTrades(authenticatedUser, belongsToMe);

        if (trades.isEmpty()) {
            return new Response(
                    HttpStatus.NO_CONTENT,
                    ContentType.JSON,
                    getObjectMapper().writeValueAsString(trades)
            );
        }

        return new Response(
                HttpStatus.OK,
                ContentType.JSON,
                getObjectMapper().writeValueAsString(trades)
        );
    }

    public Response postTrades(Request request, User authenticatedUser) throws JsonProcessingException {
        try {
            Trade trade = getObjectMapper().readValue(request.getBody(), Trade.class);
            List<Card> cardsFromUser = cardService.getCardsFromUser(authenticatedUser);
            List<Card> cardsFromDeck = cardService.getCardsFromDeck(authenticatedUser);

            tradingService.postTrades(trade, cardsFromUser, cardsFromDeck, authenticatedUser);
            tradeRepository.insert(trade);

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    getObjectMapper().writeValueAsString(trade)
            );
        } catch (NotAvailableException e) {
            return new Response(
                    HttpStatus.FORBIDDEN,
                    ContentType.JSON,
                    "{ \"error\": \"The deal contains a card that is not owned by the user or locked in the deck!\"}"
            );
        } catch (ConflictException e) {
            return new Response(
                    HttpStatus.CONFLICT,
                    ContentType.JSON,
                    "{ \"error\": \"A deal with this deal ID already exists!\"}"
            );
        }
    }

    public Response deleteTrade(User authenticatedUser, String tradeId) throws JsonProcessingException {
        try {
            tradingService.deleteTrade(tradeId, authenticatedUser);
            tradeRepository.delete(tradeId);

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    getObjectMapper().writeValueAsString("Deleted.")
            );
        } catch (NotAvailableException e) {
            return new Response(
                    HttpStatus.NOT_FOUND,
                    ContentType.JSON,
                    "{ \"error\": \"The provided deal ID was not found.\"}"
            );
        } catch (ConflictException e) {
            return new Response(
                    HttpStatus.FORBIDDEN,
                    ContentType.JSON,
                    "{ \"error\": \"The deal contains a card that is not owned by the user.\"}"
            );
        }
    }

    public Response performTrade(Request request, User authenticatedUser, String tradeId) throws JsonProcessingException {
        try {
            String cardId = getObjectMapper().readValue(request.getBody(), String.class);
            List<Card> cardsFromUser = cardService.getCardsFromUser(authenticatedUser);
            List<Card> cardsFromDeck = cardService.getCardsFromDeck(authenticatedUser);

            List<Object> result = tradingService.performTrade(tradeId, cardId, cardsFromUser, cardsFromDeck, authenticatedUser);
            tradeRepository.performTrade(result);

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    getObjectMapper().writeValueAsString("Success")
            );
        } catch (NotAvailableException e) {
            return new Response(
                    HttpStatus.CONFLICT,
                    ContentType.JSON,
                    "{ \"error\": \"The provided deal ID was not found!\"}"
            );
        } catch (ConflictException e) {
            return new Response(
                    HttpStatus.FORBIDDEN,
                    ContentType.JSON,
                    "{ \"error\": \"The offered card is not owned by the user, " +
                            "or the requirements are not met (Type, MinimumDamage), " +
                            "or the offered card is locked in the deck, " +
                            "or the user tries to trade with self!\"}"
            );
        }
    }

    public void loadAll(List<User> users, List<Card> cards) {
        List<Trade> tradeList = getTradeRepository().loadAll(users, cards);
        getTradingService().getAllTrades().addAll(tradeList);
    }
}
