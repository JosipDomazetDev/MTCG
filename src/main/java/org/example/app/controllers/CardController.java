package org.example.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.models.Card;
import org.example.app.models.Package;
import org.example.app.models.User;
import org.example.app.repositories.CardRepository;
import org.example.app.services.CardService;
import org.example.app.services.TradingService;
import org.example.app.services.exceptions.NotAvailableException;
import org.example.app.services.exceptions.WrongCardAmountException;
import org.example.http.ContentType;
import org.example.http.HttpStatus;
import org.example.server.Request;
import org.example.server.Response;

import java.util.List;
import java.util.Objects;

public class CardController extends Controller {
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private CardService cardService;

    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private TradingService tradingService;

    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private CardRepository CardRepository;

    public CardController(CardService cardService, TradingService tradingService, CardRepository cardRepository) {
        setCardService(cardService);
        setTradingService(tradingService);
        setCardRepository(cardRepository);
    }

    public Response getCards(User authenticatedUser) throws JsonProcessingException {
        List<Card> cards = cardService.getCardsFromUser(authenticatedUser);

        if (cards.isEmpty()) {
            return new Response(
                    HttpStatus.NO_CONTENT,
                    ContentType.JSON,
                    getObjectMapper().writeValueAsString(cards)
            );
        }

        return new Response(
                HttpStatus.OK,
                ContentType.JSON,
                getObjectMapper().writeValueAsString(cards)
        );
    }

    public Response getCardsFromDeck(User authenticatedUser, boolean plainMode) throws JsonProcessingException {
        List<Card> cards = cardService.getCardsFromDeck(authenticatedUser);

        if (cards.isEmpty()) {
            return new Response(
                    HttpStatus.NO_CONTENT,
                    ContentType.JSON,
                    getObjectMapper().writeValueAsString(cards)
            );
        }

        List<?> responseList;

        if (plainMode) {
            responseList = cards.stream().map(Card::toString).toList();
        } else {
            responseList = cards;
        }

        return new Response(
                HttpStatus.OK,
                ContentType.JSON,
                getObjectMapper().writeValueAsString(responseList)
        );
    }


    public Response putCardsIntoDeck(Request request, User authenticatedUser) throws JsonProcessingException {
        List<String> cardIds = getObjectMapper().readValue(request.getBody(), new TypeReference<>() {
        });

        try {
            cardService.putCardsIntoDeck(cardIds, authenticatedUser, tradingService.getAllTrades());
        } catch (WrongCardAmountException e) {
            return new Response(
                    HttpStatus.BAD_REQUEST,
                    ContentType.JSON,
                    "{ \"error\": \"Provided deck did not include the right amount of cards.\"}"
            );
        } catch (NotAvailableException e) {
            return new Response(
                    HttpStatus.FORBIDDEN,
                    ContentType.JSON,
                    "{ \"error\": \"At least one of the provided cards does not belong to the user or is not available.\"}"
            );
        }

        getCardRepository().updateDeck(authenticatedUser.getDeck());

        return new Response(
                HttpStatus.OK,
                ContentType.JSON,
                "\"Deck updated!\""
        );
    }


    public Response getCard(User authenticatedUser, String cardId) throws JsonProcessingException {
        Card foundCard = getCardService().getCardsFromUser(authenticatedUser).
                stream()
                .filter(card -> Objects.equals(card.getId(), cardId))
                .findFirst()
                .orElse(null);

        if (foundCard == null) {
            return new Response(
                    HttpStatus.FORBIDDEN,
                    ContentType.JSON,
                    "{ \"error\": \"This card does not belong to this user.\"}"
            );
        }

        return new Response(
                HttpStatus.OK,
                ContentType.JSON,
                getObjectMapper().writeValueAsString(foundCard)
        );
    }

    public void loadAll(List<User> users) {
        List<Package> packList = getCardRepository().loadAll(users);
        cardService.getPackages().addAll(packList);
    }
}
