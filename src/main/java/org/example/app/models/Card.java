package org.example.app.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.beans.ConstructorProperties;

@Getter
@Setter
public class Card {
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private String id;
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private String name;
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private double damage;

    @JsonIgnore
    private Package pack;
    @JsonIgnore
    private ElementType elementType;
    @JsonIgnore
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

        if (name.contains("Water")) {
            this.elementType = ElementType.WATER;
        } else if (name.contains("Fire")) {
            this.elementType = ElementType.FIRE;
        } else {
            this.elementType = ElementType.NORMAL;
        }
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", name, damage);
    }

    public static final String DRAGON = "Dragon";
    public static final String GOBLINS = "Goblin";
    public static final String ORK = "Ork";
    public static final String KNIGHT = "Knight";
    public static final String WIZARD = "Wizzard";
    public static final String WATER_SPELL = "WaterSpell";
    public static final String KRAKEN = "Kraken";
    public static final String FIRE_ELF = "FireElf";

    public boolean isDragon() {
        return name.contains(DRAGON);
    }

    public boolean isGoblin() {
        return name.contains(GOBLINS);
    }

    public boolean isWizard() {
        return name.contains(WIZARD);
    }

    public boolean isOrk() {
        return name.contains(ORK);
    }

    public boolean isKnight() {
        return name.contains(KNIGHT);
    }

    public boolean isWaterSpell() {
        return name.contains(WATER_SPELL);
    }

    public boolean isKraken() {
        return name.contains(KRAKEN);
    }

    public boolean isFireElf() {
        return name.contains(FIRE_ELF);
    }

    public boolean isSpell() {
        return cardType == CardType.SPELL;
    }

    public boolean isWater() {
        return elementType == ElementType.WATER;
    }

    public boolean isFire() {
        return elementType == ElementType.FIRE;
    }

    public boolean isNormal() {
        return elementType == ElementType.NORMAL;
    }
}
