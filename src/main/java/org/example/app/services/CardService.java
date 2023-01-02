package org.example.app.services;

import lombok.AccessLevel;
import lombok.Setter;
import org.example.app.models.Card;
import org.example.app.models.Package;
import org.example.app.models.User;
import org.example.app.services.exceptions.NoMoneyException;
import org.example.app.services.exceptions.NotAvailableException;

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

    public Package buyPackage(User authenticatedUser) throws NotAvailableException, NoMoneyException {
        List<Package> unsoldPackages = packages.stream().filter(aPackage -> aPackage.getUser() == null).toList();

        if (unsoldPackages.isEmpty()) {
            throw new NotAvailableException();
        }

        Package packageToBeBought = unsoldPackages.get(0);
        authenticatedUser.buyPackage(packageToBeBought);

        return packageToBeBought;
    }
}
