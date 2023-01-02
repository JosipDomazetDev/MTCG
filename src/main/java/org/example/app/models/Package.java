package org.example.app.models;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class Package {
    private final static double PACKAGE_PRICE = 5.0;
    private String id;
    private List<Card> cards;

    public Package(ArrayList<Card> cards) {
        this.id = UUID.randomUUID().toString();
        this.cards = cards;

        cards.forEach(card -> card.setPack(this));
    }


}
