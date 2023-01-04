package org.example.app.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

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

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    @JsonIgnore
    private CardType type;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private float minimumDamage;

    private boolean isCompleted;

    @JsonIgnore
    private Card card;
    @JsonIgnore
    private User user;

    @JsonProperty("type")
    private String getType() {
        return type == CardType.SPELL ? "Spell" : "Monster";
    }

    @ConstructorProperties({"id", "cardToTrade", "type", "minimumDamage"})
    public Trade(String id, String cardToTrade, String type, float minimumDamage) {
        this.id = id;
        this.cardToTrade = cardToTrade;

        if (type.toLowerCase().contains("spell")) {
            this.type = CardType.SPELL;
        } else {
            this.type = CardType.MONSTER;
        }
        this.minimumDamage = minimumDamage;
    }

    public void finalizeTrade(List<Card> cardsFromUser, List<Card> cardsFromDeck, User authenticatedUser) {
        List<String> cardIdsFromDeck = cardsFromDeck.stream().map(Card::getId).toList();

        card = cardsFromUser.stream().filter(card1 -> {
            // Check desire cardId is owned by user and not in deck.
            return Objects.equals(card1.getId(), cardToTrade) && !cardIdsFromDeck.contains(cardToTrade);
        }).findFirst().orElse(null);

        this.user = authenticatedUser;
    }
}
