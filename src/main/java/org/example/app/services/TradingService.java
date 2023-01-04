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

public class TradingService {
    @Setter(AccessLevel.PRIVATE)
    private List<Trade> trades;

    public TradingService() {
        setTrades(new ArrayList<>());
    }

    public List<Trade> getTrades(User authenticatedUser) {
        // get Trades that aren't from the user himself
        return trades.stream()
                .filter(trade -> !Objects.equals(trade.getUser().getId(), authenticatedUser.getId()))
                .toList();
    }

    public void postTrades(Trade trade, List<Card> cardsFromUser, List<Card> cardsFromDeck, User authenticatedUser) throws ConflictException, NotAvailableException {
        if (trades.stream().anyMatch(t -> Objects.equals(t.getId(), trade.getId()))) {
            throw new ConflictException();
        }

        trade.finalizeTrade(cardsFromUser, cardsFromDeck, authenticatedUser);

        if (trade.getCard() == null) {
            throw new NotAvailableException();
        }

        trades.add(trade);
    }
}
