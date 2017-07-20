package com.thrashplay.merchants.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2016 Sean Kleinjung to present.
 * All rights reserved.
 */
public class Hand {
    private List<Card> cards = new ArrayList<>(27);

    public List<Card> getCards() {
        return cards;
    }

    public void add(Card card) {
        cards.add(card);
    }

    public void discard(DiscardPile discardPile, List<Card> cards) {
        discardPile.addAll(cards);
        this.cards.removeAll(cards);
    }
}
