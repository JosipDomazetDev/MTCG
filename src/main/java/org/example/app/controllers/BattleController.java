package org.example.app.controllers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.models.User;
import org.example.app.services.BattleService;
import org.example.app.services.CardService;
import org.example.http.ContentType;
import org.example.http.HttpStatus;
import org.example.server.Response;

public class BattleController extends Controller{
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private BattleService battleService;

    public BattleController(BattleService battleService) {
        setBattleService(battleService);
    }

    public Response createOrStartBattle(User authenticatedUser) {
        String battleLog = battleService.createOrStartBattle(authenticatedUser);
        System.out.println(battleLog);
        return new Response(
                HttpStatus.OK,
                ContentType.JSON,
                battleLog
        );
    }
}
