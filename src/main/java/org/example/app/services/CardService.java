package org.example.app.services;

import lombok.AccessLevel;
import lombok.Setter;
import org.example.app.models.Card;
import org.example.app.models.Package;
import org.example.app.models.User;

import java.util.ArrayList;
import java.util.List;

public class CardService {
    @Setter(AccessLevel.PRIVATE)
    private List<Package> packages;

    public CardService() {
        setPackages(new ArrayList<>());
    }

    public void createPackageWithCards(ArrayList<Card> cards, User authenticatedUser) {
        Package pack = new Package(cards);
        packages.add(pack);
    }
}
