package org.example.app.controllers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.models.Battle;
import org.example.app.models.User;
import org.example.app.repositories.BattleRepository;
import org.example.app.services.BattleService;
import org.example.app.services.exceptions.ConflictException;
import org.example.http.ContentType;
import org.example.http.HttpStatus;
import org.example.server.Response;

import java.util.List;

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

    public Response createOrStartBattle(User authenticatedUser) throws InterruptedException {
        Battle battle;
        try {
            battle = battleService.createOrStartBattle(authenticatedUser);
        } catch (ConflictException e) {
            return new Response(
                    HttpStatus.CONFLICT,
                    ContentType.JSON,
                    "{ \"error\": \"You cannot play against yourself!\"}"
            );
        }

        boolean storeBattle = true;
        int sleepCount = 0;
        boolean dismantleBattle = false;

        while (!battle.isCompleted()) {
            // No need to store, the request of the second user will do that
            storeBattle = false;

            // Wait 10 seconds, if no one joins dismantle the party
            sleepCount++;
            if (sleepCount >= 10) {
                dismantleBattle = true;
                break;
            }

            Thread.sleep(1000);
        }

        if (dismantleBattle) {
            battleService.dismantleBattle(battle);
            System.out.println("Dismantled the party of " + authenticatedUser.getUsername());

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    "{ \"error\": \"No one joined your lobby. Try again :/\"}"
            );
        }

        String battleLog = battle.getBattleLog().toString();

        if (storeBattle) {
            System.out.println(battleLog);
            battleRepository.insert(battle);
        }

        return new Response(
                HttpStatus.OK,
                ContentType.JSON,
                battleLog
        );
    }

    public void loadAll(List<User> users) {
        List<Battle> battleList = getBattleRepository().loadAll(users);
        getBattleService().getBattles().addAll(battleList);
    }
}
