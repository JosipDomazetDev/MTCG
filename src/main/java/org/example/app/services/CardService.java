package org.example.app.services;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.models.Card;
import org.example.app.models.Package;
import org.example.app.models.Trade;
import org.example.app.models.User;
import org.example.app.services.exceptions.NoMoneyException;
import org.example.app.services.exceptions.NotAvailableException;
import org.example.app.services.exceptions.WrongCardAmountException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CardService {
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PUBLIC)
    private List<Package> packages;

    public CardService() {
        setPackages(Collections.synchronizedList(new ArrayList<>()));
    }

    public Package createPackageWithCards(ArrayList<Card> cards, User authenticatedUser) {
        Package pack = new Package(cards);
        packages.add(pack);
        return pack;
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
        return packages.stream()
                .flatMap(aPackage -> aPackage.getCards().stream())
                .filter(card ->
                        card.getOwner() != null && Objects.equals(card.getOwner().getId(), authenticatedUser.getId())).toList();
    }

    public List<Card> getCardsFromDeck(User authenticatedUser) {
        return authenticatedUser.getDeck().getCards();
    }

    public void putCardsIntoDeck(List<String> cardIds, User authenticatedUser, List<Trade> trades) throws WrongCardAmountException, NotAvailableException {
        if (cardIds.size() != 4) {
            throw new WrongCardAmountException();
        }

        // Also checking if card is locked in any ongoing trade:
        List<String> cardIdsInActiveTrades = trades.stream()
                .filter(trade -> !trade.isCompleted())
                .map(trade -> trade.getCard().getId()).toList();

        if (cardIds.stream().anyMatch(cardIdsInActiveTrades::contains)) {
            throw new NotAvailableException();
        }

        List<Card> cards = getCardsFromUser(authenticatedUser).stream().filter(card -> cardIds.contains(card.getId())).toList();

        if (cards.size() != 4) {
            throw new NotAvailableException();
        }

        authenticatedUser.getDeck().setCards(cards);
    }
}
