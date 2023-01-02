package org.example.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.models.Card;
import org.example.app.models.Package;
import org.example.app.models.User;
import org.example.app.services.CardService;
import org.example.app.services.UserService;
import org.example.http.ContentType;
import org.example.http.HttpStatus;
import org.example.server.Request;
import org.example.server.Response;

import java.util.ArrayList;
import java.util.List;

public class PackageController extends Controller {
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private CardService cardService;

    public PackageController(CardService cardService) {
        setCardService(cardService);
    }

    public Response createPackage(Request request, User authenticatedUser) throws JsonProcessingException {
        ArrayList<Card> cards = getObjectMapper().readValue(request.getBody(), new TypeReference<>() {
        });
        cardService.createPackageWithCards(cards, authenticatedUser);


        return new Response(
                HttpStatus.OK,
                ContentType.JSON,
                "{ \"data\": \"Cards created!\", \"error\": null }"
        );
    }

    public Response buyPackage(Request request, User authenticatedUser) {

        return null;
    }
}
