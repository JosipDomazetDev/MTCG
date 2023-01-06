package org.example.app.services;

import lombok.AccessLevel;
import lombok.Setter;
import org.example.app.models.Battle;
import org.example.app.models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BattleService {
    @Setter(AccessLevel.PRIVATE)
    private List<Battle> battles;

    public BattleService() {
        setBattles(Collections.synchronizedList(new ArrayList<>()));
    }

    public Battle createOrStartBattle(User authenticatedUser) {
        List<Battle> freeBattles = getFreeBattles();

        if (freeBattles.isEmpty()) {
            // Create new Battle
            battles.add(new Battle(authenticatedUser));

            return null;
        } else {
            Battle battle = freeBattles.get(0);
            battle.setPlayer2(authenticatedUser);
            battle.finishBattle();

            return battle;
        }
    }

    private List<Battle> getFreeBattles() {
        return battles.stream().filter(battle -> !battle.isCompleted() && battle.getPlayer2() == null).toList();
    }

}
