package org.example.app.services;

import lombok.AccessLevel;
import lombok.Getter;
import org.example.app.models.Card;
import org.example.app.models.Package;
import org.example.app.models.Trade;
import org.example.app.models.User;
import org.example.app.services.exceptions.ConflictException;
import org.example.app.services.exceptions.NoMoneyException;
import org.example.app.services.exceptions.NotAvailableException;
import org.example.app.services.exceptions.WrongCardAmountException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CardService {
    @Getter(AccessLevel.PUBLIC)
    private final List<Package> packages = Collections.synchronizedList(new ArrayList<>());

    public CardService() {

    }

    public Package createPackageWithCards(ArrayList<Card> cards, User authenticatedUser) throws ConflictException {
        List<String> allCardIds = packages.stream().flatMap(aPackage -> aPackage.getCards().stream()).toList()
                .stream().map(Card::getId).toList();
        List<String> cardIds = cards.stream().map(Card::getId).toList();

        if (allCardIds.stream().anyMatch(cardIds::contains)) {
            throw new ConflictException();
        }

        Package pack = new Package(cards);
        synchronized (packages) {
            packages.add(pack);
        }
        return pack;
    }

    public Package buyPackage(User authenticatedUser) throws NotAvailableException, NoMoneyException {
        List<Package> unsoldPackages = packages.stream().filter(aPackage -> aPackage.getUser() == null).toList();

        if (unsoldPackages.isEmpty()) {
            throw new NotAvailableException();
        }

        Package packageToBeBought = unsoldPackages.get(0);
        synchronized (packageToBeBought){
            authenticatedUser.buyPackage(packageToBeBought);
        }

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

        List<Card> deckCards = authenticatedUser.getDeck().getCards();
        synchronized (deckCards){
            deckCards.clear();
            deckCards.addAll(cards);
        }
    }

    public List<Card> getAllCards() {
        return getPackages().stream().flatMap(aPackage -> aPackage.getCards().stream()).toList();
    }
}
