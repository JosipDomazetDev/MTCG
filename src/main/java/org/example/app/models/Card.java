package org.example.app.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.beans.ConstructorProperties;
import java.util.UUID;

@Getter
@Setter
public class Card {
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private String id;
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private String name;
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private double damage;
    private Package pack;

    private ElementType elementType;
    private CardType cardType;

    @ConstructorProperties({"id", "name", "damage"})
    public Card(String id, String name, double damage) {
        this.id = id;
        this.name = name;
        this.damage = damage;

        if (name.contains("Spell")) {
            this.cardType = CardType.SPELL;
        } else {
            this.cardType = CardType.MONSTER;
        }
    }
}
