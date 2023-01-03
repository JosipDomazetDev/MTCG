package org.example.app.models;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class Deck {
    private User user;
    private List<Card> cards = new ArrayList<>();

    public Deck(User user) {
        this.user = user;
    }
}
