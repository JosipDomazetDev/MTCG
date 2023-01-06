package org.example.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.models.Card;
import org.example.app.models.Package;
import org.example.app.models.User;
import org.example.app.repositories.PackageRepository;
import org.example.app.services.CardService;
import org.example.app.services.exceptions.NoMoneyException;
import org.example.app.services.exceptions.NotAvailableException;
import org.example.http.ContentType;
import org.example.http.HttpStatus;
import org.example.server.Request;
import org.example.server.Response;

import java.util.ArrayList;

public class PackageController extends Controller {
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private CardService cardService;
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    PackageRepository packageRepository;

    public PackageController(CardService cardService, PackageRepository packageRepository) {
        setCardService(cardService);
        setPackageRepository(packageRepository);
    }

    public Response createPackage(Request request, User authenticatedUser) throws JsonProcessingException {
        ArrayList<Card> cards = getObjectMapper().readValue(request.getBody(), new TypeReference<>() {
        });
        Package pack = cardService.createPackageWithCards(cards, authenticatedUser);

        packageRepository.add(pack);

        return new Response(
                HttpStatus.OK,
                ContentType.JSON,
                "\"Package created!\""
        );
    }

    public Response buyPackage(User authenticatedUser) throws JsonProcessingException {
        try {
            Package pack = cardService.buyPackage(authenticatedUser);

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    getObjectMapper().writeValueAsString(pack.getCards())
            );
        } catch (NotAvailableException e) {
            return new Response(
                    HttpStatus.NOT_FOUND,
                    ContentType.JSON,
                    "{ \"error\": \"No card package available for buying!\"}"
            );
        } catch (NoMoneyException e) {
            return new Response(
                    HttpStatus.FORBIDDEN,
                    ContentType.JSON,
                    "{ \"error\": \"Not enough money for buying a card package!\"}"
            );
        }
    }
}
