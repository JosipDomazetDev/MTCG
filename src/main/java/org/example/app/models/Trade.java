package org.example.app.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.example.app.models.enums.CardType;

import java.beans.ConstructorProperties;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class Trade {
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private String id;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private String cardToTrade;

    @JsonIgnore
    private CardType cardType;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private double minimumDamage;

    @JsonIgnore
    private Card card;
    @JsonIgnore
    private User user1;
    @JsonIgnore
    private User user2;

    public Trade(String id, Card card, String cardType, double minimumDamage, User user1, User user2) {
        this.id = id;
        this.cardToTrade = card.getId();
        this.card = card;

        if (cardType.contains(CardType.SPELL.toString())) {
            this.cardType = CardType.SPELL;
        } else if (cardType.contains(CardType.MONSTER.toString())) {
            this.cardType = CardType.MONSTER;
        }

        this.minimumDamage = minimumDamage;
        this.user1 = user1;
        this.user2 = user2;
    }

    @JsonProperty("type")
    private String getJsonType() {
        return cardType == CardType.SPELL ? "Spell" : "Monster";
    }

    @ConstructorProperties({"id", "cardToTrade", "type", "minimumDamage"})
    public Trade(String id, String cardToTrade, String cardType, float minimumDamage) {
        this.id = id;
        this.cardToTrade = cardToTrade;

        if (cardType.toLowerCase().contains("spell")) {
            this.cardType = CardType.SPELL;
        } else {
            this.cardType = CardType.MONSTER;
        }
        this.minimumDamage = minimumDamage;
    }

    public void initTrade(List<Card> cardsFromUser, List<Card> cardsFromDeck, User authenticatedUser) {
        List<String> cardIdsFromDeck = cardsFromDeck.stream().map(Card::getId).toList();

        card = cardsFromUser.stream().filter(card1 -> {
            // Check desire cardId is owned by user and not in deck.
            return Objects.equals(card1.getId(), cardToTrade) && !cardIdsFromDeck.contains(cardToTrade);
        }).findFirst().orElse(null);

        this.user1 = authenticatedUser;
    }

    public synchronized void complete(User authenticatedUser) {
        user2 = authenticatedUser;
    }

    @JsonIgnore
    public boolean isCompleted() {
        return user2 != null;
    }
}
