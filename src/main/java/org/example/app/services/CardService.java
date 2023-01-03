package org.example.app.services;

import lombok.AccessLevel;
import lombok.Setter;
import org.example.app.models.Card;
import org.example.app.models.Package;
import org.example.app.models.User;
import org.example.app.services.exceptions.NoMoneyException;
import org.example.app.services.exceptions.NotAvailableException;
import org.example.app.services.exceptions.WrongCardAmountException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public List<Card> getCardsFromUser(User authenticatedUser) {
        List<Package> packagesForUser = packages.stream()
                .filter(aPackage ->
                        aPackage.getUser() != null && Objects.equals(aPackage.getUser().getId(), authenticatedUser.getId())).toList();

        return packagesForUser.stream()
                .flatMap(aPackage -> aPackage.getCards().stream()).toList();
    }

    public List<Card> getCardsFromDeck(User authenticatedUser) {
        return authenticatedUser.getDeck().getCards();
    }

    public void putCardsIntoDeck(List<String> cardIds, User authenticatedUser) throws WrongCardAmountException, NotAvailableException {
        if(cardIds.size() != 4) {
            throw new WrongCardAmountException();
        }

        List<Card> cards = getCardsFromUser(authenticatedUser).stream().filter(card -> cardIds.contains(card.getId())).toList();

        if (cards.size() != 4) {
            throw new NotAvailableException();
        }

        authenticatedUser.getDeck().setCards(cards);
    }
}
