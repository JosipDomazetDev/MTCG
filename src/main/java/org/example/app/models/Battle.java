package org.example.app.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static org.example.app.models.BattleOutcome.*;

@Getter
@Setter
public class Battle {
    User player1;
    User player2;
    StringBuilder battleLog = new StringBuilder();

    @JsonIgnore
    BattleOutcome battleOutcome;

    public Battle(User authenticatedUser) {
        this.player1 = authenticatedUser;
    }


    public boolean isCompleted() {
        return battleOutcome != null;
    }


    // Returns the winner of the battle between two cards
    public Card battle(Card card1, Card card2) {
        double card1Modifier = 1.0;
        double card2Modifier = 1.0;


        // Goblins are too afraid of Dragons to attack
        if (card1.isDragon() && card2.isGoblin()) {
            battleLog.append(getFormat("%s's \"%s\" WINS against %s's \"%s\" because the goblin was too afraid to attack the dragon.\n", getPlayer1(), card1, getPlayer2(), card2));
            return card1;
        }
        if (card1.isGoblin() && card2.isDragon()) {
            battleLog.append(getFormat("%s's \"%s\" WINS against %s's \"%s\" because the goblin was too afraid to attack the dragon.\n", getPlayer2(), card2, getPlayer1(), card1));
            return card2;
        }

        // Wizzard can control Orks, so they are not able to damage them.
        if (card1.isWizard() && card2.isOrk()) {
            battleLog.append(getFormat("%s's \"%s\" WINS against %s's \"%s\" because the wizzards control orks.\n", getPlayer1(), card1, getPlayer2(), card2));
            return card1;
        }
        if (card1.isOrk() && card2.isWizard()) {
            battleLog.append(getFormat("%s's \"%s\" WINS against %s's \"%s\" because the wizzards control orks.\n", getPlayer2(), card2, getPlayer1(), card1));
            return card2;
        }

        // The armor of Knights is so heavy that WaterSpells make them drown them instantly.
        if (card1.isKnight() && card2.isWaterSpell()) {
            battleLog.append(getFormat("%s's \"%s\" WINS against %s's \"%s\" because armor of knights is so heavy that water spells make them drown them instantly.\n", getPlayer2(), card2, getPlayer1(), card1));

            return card2;
        }
        if (card1.isWaterSpell() && card2.isKnight()) {
            battleLog.append(getFormat("%s's \"%s\" WINS against %s's \"%s\" because armor of knights is so heavy that water spells make them drown them instantly.\n", getPlayer1(), card1, getPlayer2(), card2));
            return card1;
        }

        // The Kraken is immune against spells.
        if (card1.isKraken() && card2.isSpell()) {
            battleLog.append(getFormat("%s's \"%s\" WINS against %s's \"%s\" because the the kraken is immune against spells.\n", getPlayer1(), card1, getPlayer2(), card2));
            return card1;
        }
        if (card2.isKraken() && card1.isSpell()) {
            battleLog.append(getFormat("%s's \"%s\" WINS against %s's \"%s\" because the the kraken is immune against spells.\n", getPlayer2(), card2, getPlayer1(), card1));
            return card2;
        }

        // The FireElves know Dragons since they were little and can evade their attacks.
        if (card1.isFireElf() && card2.isDragon()) {
            battleLog.append(getFormat("%s's \"%s\" WINS against %s's \"%s\" because fire elves evade dragons attacks.\n", getPlayer1(), card1, getPlayer2(), card2));
            return card1;
        }
        if (card1.isDragon() && card2.isFireElf()) {
            battleLog.append(getFormat("%s's \"%s\" WINS against %s's \"%s\" because fire elves evade dragons attacks.\n", getPlayer2(), card2, getPlayer1(), card1));
            return card2;
        }

        // The element type does not affect pure monster fights.
        if (card1.isSpell() || card2.isSpell()) {
            if (card1.isWater() && card2.isFire()) {
                card1Modifier = 2;
                card2Modifier = 0.5;
            } else if (card1.isFire() && card2.isNormal()) {
                card1Modifier = 2;
                card2Modifier = 0.5;
            } else if (card1.isNormal() && card2.isWater()) {
                card1Modifier = 2;
                card2Modifier = 0.5;
            } else if (card2.isWater() && card1.isFire()) {
                card2Modifier = 2;
                card1Modifier = 0.5;
            } else if (card2.isFire() && card1.isNormal()) {
                card2Modifier = 2;
                card1Modifier = 0.5;
            } else if (card2.isNormal() && card1.isWater()) {
                card2Modifier = 2;
                card1Modifier = 0.5;
            }
        }


        // Compare the modified damage to the two cards to determine the winner
        double newCard1Damage = card1.getDamage() * card1Modifier;
        double newCard2Damage = card2.getDamage() * card2Modifier;

        if (newCard1Damage > newCard2Damage) {
            battleLog.append(String.format("%s's \"%s\" [%.0f] WINS against %s's \"%s\" [%.0f]\n",
                    getPlayer1().getUsername(),
                    card1.getName(),
                    card1.getDamage(),
                    getPlayer2().getUsername(),
                    card2.getName(),
                    card2.getDamage()
            ));

            return card1;
        } else if (newCard2Damage > newCard1Damage) {
            battleLog.append(String.format("%s's \"%s\" [%.0f] WINS against %s's \"%s\" [%.0f]\n",
                    getPlayer2().getUsername(),
                    card2.getName(),
                    card2.getDamage(),
                    getPlayer1().getUsername(),
                    card1.getName(),
                    card1.getDamage()
            ));

            return card2;
        } else {
            battleLog.append(String.format("%s's \"%s\" [%.0f] DRAWS against %s's \"%s\" [%.0f]\n",
                    getPlayer1().getUsername(),
                    card1.getName(),
                    card1.getDamage(),
                    getPlayer2().getUsername(),
                    card2.getName(),
                    card2.getDamage()
            ));
            // Draw
            return null;
        }
    }

    private String getFormat(String format, User player1, Card card1, User player2, Card card2) {
        return String.format(format,
                player1.getUsername(),
                card1.getName(),
                player2.getUsername(),
                card2.getName()
        );
    }

    public StringBuilder finishBattle() {
        battleLog.append("=====================================\n");
        battleLog.append(String.format("Let's Duel, %s [Elo: %d] vs. %s [Elo: %d]\n\n",
                getPlayer1().getUsername(),
                getPlayer1().getStat().getElo(),
                getPlayer2().getUsername(),
                getPlayer2().getStat().getElo()));

        // Copy the decks because we move the cards around
        List<Card> deck1 = new ArrayList<>(getPlayer1().getDeck().getCards());
        List<Card> deck2 = new ArrayList<>(getPlayer2().getDeck().getCards());


        int i = 0;
        Random rand = new Random();
        while (!deck1.isEmpty() && !deck2.isEmpty() && i < 100) {
            // Draw a random card
            int index1 = rand.nextInt(deck1.size());
            int index2 = rand.nextInt(deck2.size());

            Card card1 = deck1.get(index1);
            Card card2 = deck2.get(index2);

            Card winnerCard = battle(card1, card2);

            // null means draw
            if (winnerCard != null) {
                if (Objects.equals(winnerCard.getId(), card1.getId())) {
                    // If player 1 wins round
                    deck2.remove(card2);
                    deck1.add(card2);
                } else if (Objects.equals(winnerCard.getId(), card2.getId())) {
                    // If player 2 wins round
                    deck1.remove(card1);
                    deck2.add(card1);
                }
            }

            i++;
        }


        if (deck1.isEmpty()) {
            player1.getStat().lost();
            player2.getStat().won();
            setBattleOutcome(WIN_PLAYER_2);

            battleLog.append(String.format("\n%s [Elo: %d] WINS AGAINST %s [Elo: %d]",
                    getPlayer1().getUsername(),
                    getPlayer1().getStat().getElo(),
                    getPlayer2().getUsername(),
                    getPlayer2().getStat().getElo()));
        } else if (deck2.isEmpty()) {
            player1.getStat().won();
            player2.getStat().lost();
            setBattleOutcome(WIN_PLAYER_1);

            battleLog.append(String.format("\n%s [Elo: %d] WINS AGAINST %s [Elo: %d]",
                    getPlayer2().getUsername(),
                    getPlayer2().getStat().getElo(),
                    getPlayer1().getUsername(),
                    getPlayer1().getStat().getElo())
            );
        } else {
            player1.getStat().draw();
            player2.getStat().draw();
            setBattleOutcome(DRAW);

            battleLog.append(String.format("\n%s [Elo: %d] DRAWS AGAINST %s [Elo: %d]",
                    getPlayer1().getUsername(),
                    getPlayer1().getStat().getElo(),
                    getPlayer2().getUsername(),
                    getPlayer2().getStat().getElo()));
        }

        battleLog.append("\n=====================================\n");


        return battleLog;
    }
}
