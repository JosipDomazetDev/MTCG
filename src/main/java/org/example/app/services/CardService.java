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
import java.util.Objects;
import java.util.stream.Collectors;

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

    public List<Card> getCards(User authenticatedUser) {
        List<Package> packagesForUser = packages.stream()
                .filter(aPackage ->
                        aPackage.getUser() != null && Objects.equals(aPackage.getUser().getId(), authenticatedUser.getId())).toList();

        return packagesForUser.stream()
                .flatMap(aPackage -> aPackage.getCards().stream()).toList();
    }

    public List<Card> getCardsFromDeck(User authenticatedUser) {
        return authenticatedUser.getStack().getCards();
    }
}
