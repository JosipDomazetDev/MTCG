package org.example.app.services;

import lombok.AccessLevel;
import lombok.Setter;
import org.example.app.models.Battle;
import org.example.app.models.User;

import java.util.ArrayList;
import java.util.List;

public class BattleService {
    @Setter(AccessLevel.PRIVATE)
    private List<Battle> battles;

    public BattleService() {
        setBattles(new ArrayList<>());
    }

    public String createOrStartBattle(User authenticatedUser) {
        List<Battle> freeBattles = getFreeBattles();

        if (freeBattles.isEmpty()) {
            // Create new Battle
            battles.add(new Battle(authenticatedUser));

            return "false";
        } else {
            Battle battle = freeBattles.get(0);
            battle.setPlayer2(authenticatedUser);
            StringBuilder stringBuilder = battle.finishBattle();

            return stringBuilder.toString();
        }
    }

    private List<Battle> getFreeBattles() {
        return battles.stream().filter(battle -> !battle.isCompleted() && battle.getPlayer2() == null).toList();
    }

}
