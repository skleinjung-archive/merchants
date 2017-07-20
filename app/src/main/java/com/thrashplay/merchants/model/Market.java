package com.thrashplay.merchants.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Copyright (c) 2016 Sean Kleinjung to present.
 * All rights reserved.
 */
public class Market {
    private List<Card> cards = new ArrayList<>(8);

    public void add(Card card) {
        cards.add(card);
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public List<Card> getCardsAvailableTo(Player player) {
        List<Card> result = new LinkedList<>(getCards());
        Iterator<Card> iterator = result.iterator();
        while (iterator.hasNext()) {
            Card card = iterator.next();
            if (!card.isAvailableTo(player)) {
                iterator.remove();
            }
        }
        return result;
    }

    public int getCostToBuyFor(Player player) {
        List<Card> cards = getCardsAvailableTo(player);
        int result = 0;
        for (Card card : cards) {
            result += card.getValue();
        }
        return result;
    }

    public void moveTo(Player.Merchandise merchandise, List<Card> cards) {
        for (Card card : cards) {
            if (!this.cards.contains(card)) {
                throw new IllegalArgumentException("Card '" + card + "' is not in the market.");
            }
        }

        merchandise.getCards().addAll(cards);
        this.cards.removeAll(cards);
    }
}
