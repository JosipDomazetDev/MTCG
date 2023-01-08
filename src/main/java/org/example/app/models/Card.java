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
    @JsonIgnore
    private User owner;

    public static final String SPELL = "spell";
    public static final String WATER = "water";
    public static final String FIRE = "fire";

    @ConstructorProperties({"id", "name", "damage"})
    public Card(String id, String name, double damage) {
        this.id = id;
        this.name = name;
        this.damage = damage;

        String lowerName = name.toLowerCase();

        if (lowerName.contains(SPELL)) {
            this.cardType = CardType.SPELL;
        } else {
            this.cardType = CardType.MONSTER;
        }

        if (lowerName.contains(WATER)) {
            this.elementType = ElementType.WATER;
        } else if (lowerName.contains(FIRE)) {
            this.elementType = ElementType.FIRE;
        } else {
            this.elementType = ElementType.NORMAL;
        }
    }

    public Card(String id, String name, double damage, String elementType, String cardType, User owner, Package pack) {
        this.id = id;
        this.name = name;
        this.damage = damage;

        if (cardType.contains(CardType.SPELL.toString())) {
            this.cardType = CardType.SPELL;
        } else if (cardType.contains(CardType.MONSTER.toString())) {
            this.cardType = CardType.MONSTER;
        }

        if (elementType.contains(ElementType.WATER.toString())) {
            this.elementType = ElementType.WATER;
        } else if (elementType.contains(ElementType.FIRE.toString())) {
            this.elementType = ElementType.FIRE;
        } else if (elementType.contains(ElementType.NORMAL.toString())) {
            this.elementType = ElementType.NORMAL;
        }

        this.owner = owner;
        this.pack = pack;
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
    public static final String ELF = "Elf";

    @JsonIgnore
    public boolean isDragon() {
        return name.contains(DRAGON);
    }

    @JsonIgnore

    public boolean isGoblin() {
        return name.contains(GOBLINS);
    }

    @JsonIgnore
    public boolean isWizard() {
        return name.contains(WIZARD);
    }

    @JsonIgnore
    public boolean isOrk() {
        return name.contains(ORK);
    }

    @JsonIgnore
    public boolean isKnight() {
        return name.contains(KNIGHT);
    }

    @JsonIgnore
    public boolean isWaterSpell() {
        return name.contains(WATER_SPELL);
    }

    @JsonIgnore
    public boolean isKraken() {
        return name.contains(KRAKEN);
    }

    @JsonIgnore
    public boolean isFireElf() {
        return name.contains(FIRE_ELF);
    }

    @JsonIgnore
    public boolean isElf() {
        return name.contains(ELF);
    }

    @JsonIgnore
    public boolean isSpell() {
        return cardType == CardType.SPELL;
    }

    @JsonIgnore
    public boolean isWater() {
        return elementType == ElementType.WATER;
    }

    @JsonIgnore
    public boolean isFire() {
        return elementType == ElementType.FIRE;
    }

    @JsonIgnore
    public boolean isNormal() {
        return elementType == ElementType.NORMAL;
    }

    @JsonIgnore
    public double getCritChance() {
        if (cardType != CardType.MONSTER) return 0;

        if (isDragon()) {
            return 0.01;
        } else if (isGoblin()) {
            return 0.4;
        } else if (isKnight()) {
            return 0.2;
        } else if (isKraken()) {
            return 0.1;
        } else {
            return 0.05;
        }
    }

    @JsonIgnore
    public double getDodgeChance() {
        if (cardType != CardType.MONSTER) return 0;

        if (isDragon()) {
            return 0.10;
        } else if (isGoblin()) {
            return 0.10;
        } else if (isElf()) {
            return 0.3;
        } else {
            return 0.05;
        }
    }

    public synchronized void swapWith(Card offeredCard) {
        User temp = getOwner();
        setOwner(offeredCard.getOwner());
        offeredCard.setOwner(temp);
    }
}
