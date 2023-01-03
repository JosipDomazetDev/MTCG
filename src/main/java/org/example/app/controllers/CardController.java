package org.example.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.models.Card;
import org.example.app.models.User;
import org.example.app.services.CardService;
import org.example.app.services.exceptions.NotAvailableException;
import org.example.app.services.exceptions.WrongCardAmountException;
import org.example.http.ContentType;
import org.example.http.HttpStatus;
import org.example.server.Request;
import org.example.server.Response;

import java.util.List;

public class CardController extends Controller {
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private CardService cardService;

    public CardController(CardService cardService) {
        setCardService(cardService);
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

    public Response getCardsFromDeck(User authenticatedUser) throws JsonProcessingException {
        List<Card> cards = cardService.getCardsFromDeck(authenticatedUser);

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

    public Response putCardsIntoDeck(Request request, User authenticatedUser) throws JsonProcessingException {
        List<String> cardIds = getObjectMapper().readValue(request.getBody(), new TypeReference<>() {
        });

        try {
            cardService.putCardsIntoDeck(cardIds, authenticatedUser);
        } catch (WrongCardAmountException e) {
            return new Response(
                    HttpStatus.BAD_REQUEST,
                    ContentType.JSON,
                    "\"Provided deck did not include the right amount of cards.\""
            );
        } catch (NotAvailableException e) {
            return new Response(
                    HttpStatus.FORBIDDEN,
                    ContentType.JSON,
                    "\"At least one of the provided cards does not belong to the user or is not available.\""
            );
        }

        return new Response(
                HttpStatus.OK,
                ContentType.JSON,
                "\"Deck updated!\""
        );
    }
}
