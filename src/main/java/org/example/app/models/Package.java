package org.example.app.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class Package {
    @JsonIgnore
    private final static int PACKAGE_PRICE = 5;


    private String id;
    private List<Card> cards;

    @JsonIgnore
    private User user;
    @JsonIgnore
    private int price;

    public Package(ArrayList<Card> cards) {
        this.id = UUID.randomUUID().toString();
        this.cards = cards;
        this.price = PACKAGE_PRICE;

        cards.forEach(card -> card.setPack(this));
    }
}
