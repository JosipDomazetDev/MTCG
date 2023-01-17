package org.example.app.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.example.app.models.enums.BattleOutcome;
import org.example.app.models.enums.CardType;

import java.util.*;

import static org.example.app.models.enums.BattleOutcome.*;

@Getter
@Setter
public class Battle {
    String id;
    User player1;
    User player2;
    StringBuilder battleLog = new StringBuilder();

    @JsonIgnore
    BattleOutcome battleOutcome;

    public Battle(User authenticatedUser) {
        this.id = UUID.randomUUID().toString();
        this.player1 = authenticatedUser;
    }

    public Battle(String id, User player1, User player2, String battleLog, String battleOutcome) {
        this.id = id;
        this.player1 = player1;
        this.player2 = player2;
        this.battleLog.append(battleLog);

        if (battleOutcome.equals(WIN_PLAYER_1.toString())) {
            this.battleOutcome = WIN_PLAYER_1;
        } else if (battleOutcome.equals(WIN_PLAYER_2.toString())) {
            this.battleOutcome = WIN_PLAYER_2;
        } else if (battleOutcome.equals(DRAW.toString())) {
            this.battleOutcome = DRAW;
        }
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


        double newCard1Damage = card1.getDamage() * card1Modifier;
        double newCard2Damage = card2.getDamage() * card2Modifier;
        boolean card1Crit = false;
        boolean card2Crit = false;

        boolean isPureMonsterBattle = card1.getCardType() == CardType.MONSTER && card2.getCardType() == CardType.MONSTER;
        if (isPureMonsterBattle) {
            if (occursWithLikelihood(card1.getCritChance())) {
                newCard1Damage = newCard1Damage * 1.5;
                card1Crit = true;
            }
            if (occursWithLikelihood(card2.getCritChance())) {
                newCard2Damage = newCard2Damage * 1.5;
                card2Crit = true;
            }

        }

        // Compare the modified damage to the two cards to determine the winner
        if (newCard1Damage > newCard2Damage) {
            if (occursWithLikelihood(card2.getDodgeChance()) && isPureMonsterBattle) {
                battleLog.append(String.format("%s's \"%s\" [%.0f%s] DRAWS against %s's \"%s\" [%.0f%s] by narrowly escaping the attack\n",
                        getPlayer2().getUsername(),
                        card2.getName(),
                        newCard2Damage,
                        card2Crit ? "!" : "",
                        getPlayer1().getUsername(),
                        card1.getName(),
                        newCard1Damage,
                        card1Crit ? "!" : ""
                ));
                return null;
            }

            battleLog.append(String.format("%s's \"%s\" [%.0f%s] WINS against %s's \"%s\" [%.0f%s]\n",
                    getPlayer1().getUsername(),
                    card1.getName(),
                    newCard1Damage,
                    card1Crit ? "!" : "",
                    getPlayer2().getUsername(),
                    card2.getName(),
                    newCard2Damage,
                    card2Crit ? "!" : ""
            ));

            return card1;
        } else if (newCard2Damage > newCard1Damage) {
            if (occursWithLikelihood(card1.getDodgeChance()) && isPureMonsterBattle) {
                battleLog.append(String.format("%s's \"%s\" [%.0f%s] DRAWS against %s's \"%s\" [%.0f%s] by narrowly escaping the attack\n",
                        getPlayer1().getUsername(),
                        card1.getName(),
                        newCard1Damage,
                        card1Crit ? "!" : "",
                        getPlayer2().getUsername(),
                        card2.getName(),
                        newCard2Damage,
                        card2Crit ? "!" : ""
                ));
                return null;
            }

            battleLog.append(String.format("%s's \"%s\" [%.0f%s] WINS against %s's \"%s\" [%.0f%s]\n",
                    getPlayer2().getUsername(),
                    card2.getName(),
                    newCard2Damage,
                    card2Crit ? "!" : "",
                    getPlayer1().getUsername(),
                    card1.getName(),
                    newCard1Damage,
                    card1Crit ? "!" : ""
            ));

            return card2;
        } else {
            battleLog.append(String.format("%s's \"%s\" [%.0f%s] DRAWS against %s's \"%s\" [%.0f%s]\n",
                    getPlayer1().getUsername(),
                    card1.getName(),
                    newCard1Damage,
                    card1Crit ? "!" : "",
                    getPlayer2().getUsername(),
                    card2.getName(),
                    newCard2Damage,
                    card2Crit ? "!" : ""
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

    public synchronized void finishBattle() {
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
        Random rand = getRand();
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
                    getPlayer2().getUsername(),
                    getPlayer2().getStat().getElo(),
                    getPlayer1().getUsername(),
                    getPlayer1().getStat().getElo())
            );
        } else if (deck2.isEmpty()) {
            player1.getStat().won();
            player2.getStat().lost();
            setBattleOutcome(WIN_PLAYER_1);

            battleLog.append(String.format("\n%s [Elo: %d] WINS AGAINST %s [Elo: %d]",
                    getPlayer1().getUsername(),
                    getPlayer1().getStat().getElo(),
                    getPlayer2().getUsername(),
                    getPlayer2().getStat().getElo()));
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


    }

    private static Random random = new Random();

    public static Random getRand() {
        return random;
    }

    public boolean occursWithLikelihood(double likelihood) {
        return getRand().nextDouble() <= likelihood;
    }


}
