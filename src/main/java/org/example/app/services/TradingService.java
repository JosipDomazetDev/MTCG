package org.example.app.services;

import lombok.AccessLevel;
import lombok.Setter;
import org.example.app.models.Card;
import org.example.app.models.Trade;
import org.example.app.models.User;
import org.example.app.services.exceptions.ConflictException;
import org.example.app.services.exceptions.NotAvailableException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class TradingService {
    @Setter(AccessLevel.PRIVATE)
    private List<Trade> trades;

    public TradingService() {
        setTrades(new ArrayList<>());
    }

    public List<Trade> getTrades(User authenticatedUser, boolean belongsToMe) {
        Stream<Trade> activeStream = trades.stream()
                .filter(t -> !t.isCompleted());

        if (belongsToMe) {
            return activeStream
                    .filter(trade -> Objects.equals(trade.getUser().getId(), authenticatedUser.getId()))
                    .toList();
        }

        // get Trades that aren't from the user himself
        return activeStream
                .filter(trade -> !Objects.equals(trade.getUser().getId(), authenticatedUser.getId()))
                .toList();
    }

    public void postTrades(Trade trade, List<Card> cardsFromUser, List<Card> cardsFromDeck, User authenticatedUser) throws ConflictException, NotAvailableException {
        if (trades.stream().anyMatch(t -> Objects.equals(t.getId(), trade.getId()))) {
            throw new ConflictException();
        }

        trade.initTrade(cardsFromUser, cardsFromDeck, authenticatedUser);

        if (trade.getCard() == null) {
            throw new NotAvailableException();
        }

        trades.add(trade);
    }

    public void deleteTrade(String tradeId, User authenticatedUser) throws NotAvailableException, ConflictException {
        Trade trade = getTradeById(tradeId);

        if (trade == null) {
            throw new NotAvailableException();
        }

        if (!Objects.equals(trade.getCard().getPack().getUser().getId(), authenticatedUser.getId())) {
            //The deal contains a card that is not owned by the user.
            throw new ConflictException();
        }

        trades.remove(trade);
    }

    public void performTrade(String tradeId, String cardId, List<Card> cardsFromUser, List<Card> cardsFromDeck, User authenticatedUser) throws NotAvailableException, ConflictException {
        Trade trade = getTradeById(tradeId);

        if (trade == null) {
            //The provided deal ID was not found.
            throw new NotAvailableException();
        }

        Card offeredCard = cardsFromUser.stream().filter(card -> Objects.equals(card.getId(), cardId)).findFirst().orElse(null);

        if (offeredCard == null) {
            // The offered card is not owned by the user
            throw new ConflictException();
        }

        if (offeredCard.getDamage() < trade.getCard().getDamage() || trade.getType() != offeredCard.getCardType()) {
            // The requirements are not met (Type, MinimumDamage)
            throw new ConflictException();
        }


        if (cardsFromDeck.stream().anyMatch(card -> Objects.equals(card.getId(), offeredCard.getId()))) {
            // The offered card is locked in the deck
            throw new ConflictException();
        }

        if (Objects.equals(authenticatedUser.getId(), trade.getCard().getPack().getUser().getId())) {
            // The user tries to trade with self
            throw new ConflictException();
        }

        trade.getCard().switchWith(offeredCard);
        trade.complete(authenticatedUser);
    }

    private Trade getTradeById(String tradeId) {
        return trades.stream()
                .filter(t -> Objects.equals(t.getId(), tradeId)).findFirst().orElse(null);
    }

    public List<Trade> getAllTrades() {
        return trades;
    }
}
