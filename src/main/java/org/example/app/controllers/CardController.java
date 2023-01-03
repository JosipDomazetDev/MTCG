package org.example.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.models.Card;
import org.example.app.models.User;
import org.example.app.services.CardService;
import org.example.http.ContentType;
import org.example.http.HttpStatus;
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
       List<Card> cards = cardService.getCards(authenticatedUser);

       if(cards.isEmpty()){
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

        if(cards.isEmpty()){
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
}
