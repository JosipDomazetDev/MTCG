package org.example.app.controllers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.models.Battle;
import org.example.app.models.User;
import org.example.app.repositories.BattleRepository;
import org.example.app.services.BattleService;
import org.example.http.ContentType;
import org.example.http.HttpStatus;
import org.example.server.Response;

public class BattleController extends Controller {
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private BattleService battleService;
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private BattleRepository battleRepository;

    public BattleController(BattleService battleService, BattleRepository battleRepository) {
        setBattleService(battleService);
        setBattleRepository(battleRepository);
    }

    public Response createOrStartBattle(User authenticatedUser) {
        Battle battle = battleService.createOrStartBattle(authenticatedUser);
        String battleLog = "";

        if (battle != null) {
            battleLog = battle.getBattleLog().toString();
            System.out.println(battleLog);
            battleRepository.insert(battle);
        }

        return new Response(
                HttpStatus.OK,
                ContentType.JSON,
                battleLog
        );
    }
}
