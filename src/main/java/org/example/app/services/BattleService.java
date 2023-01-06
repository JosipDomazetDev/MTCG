package org.example.app.services;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.models.Battle;
import org.example.app.models.User;
import org.example.app.services.exceptions.ConflictException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BattleService {
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PUBLIC)
    private List<Battle> battles;

    public BattleService() {
        setBattles(Collections.synchronizedList(new ArrayList<>()));
    }

    public Battle createOrStartBattle(User authenticatedUser) throws ConflictException {
        List<Battle> freeBattles = getFreeBattles();
        Battle battle;

        if (freeBattles.isEmpty()) {
            // Create new Battle
            battle = new Battle(authenticatedUser);
            battles.add(battle);
        } else {
            battle = freeBattles.get(0);
            battle.setPlayer2(authenticatedUser);

            if (Objects.equals(battle.getPlayer1().getId(), battle.getPlayer2().getId())) {
                throw new ConflictException();
            }

            battle.finishBattle();
        }

        return battle;
    }

    private List<Battle> getFreeBattles() {
        return battles.stream().filter(battle -> !battle.isCompleted() && battle.getPlayer2() == null).toList();
    }

    public void dismantleBattle(Battle battle) {
        battles.remove(battle);
    }
}
