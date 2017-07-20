package com.thrashplay.merchants.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2016 Sean Kleinjung to present.
 * All rights reserved.
 */
public class Player {

    private Color color;
    private Card reservedCard;
    private Hand hand = new Hand();
    private Merchandise merchandise = new Merchandise();
    private Map<GoodColor, Integer> achievementTokenCounts = new HashMap<>();
    private List<Card> victoryPoints = new ArrayList<>(108);

    public Player(Color color) {
        this.color = color;

        achievementTokenCounts.put(GoodColor.Red, 0);
        achievementTokenCounts.put(GoodColor.Yellow, 0);
        achievementTokenCounts.put(GoodColor.Green, 0);
        achievementTokenCounts.put(GoodColor.Blue, 0);
    }

    public Color getColor() {
        return color;
    }

    public boolean isCardReserved() {
        return reservedCard != null;
    }

    public int getAchievementTokenCount(GoodColor color) {
        return achievementTokenCounts.get(color);
    }

    private void addAchievementToken(GoodColor color) {
        achievementTokenCounts.put(color, achievementTokenCounts.get(color) + 1);
    }

    public void reserveCard(Card card) {
        if (reservedCard != null) {
            reservedCard.setReservedBy(null);
        }

        reservedCard = card;

        if (reservedCard != null) {
            reservedCard.setReservedBy(this);
        }
    }

    public Hand getHand() {
        return hand;
    }

    public Merchandise getMerchandise() {
        return merchandise;
    }

    public int getVictoryPointCount() {
        return victoryPoints.size();
    }

    public void addVictoryPoint(Card card) {
        victoryPoints.add(card);
    }

    // returns the cards that were purchased
    public List<Card> purchase(DiscardPile discardPile, Market market, List<Card> cardsToSpend) {
        hand.discard(discardPile, cardsToSpend);
        List<Card> cardsToPurchase = market.getCardsAvailableTo(this);
        market.moveTo(merchandise, cardsToPurchase);
        return cardsToPurchase;
    }

    public void score(DiscardPile discardPile, DrawPile drawPile, GoodColor color) {
        List<Card> cards = merchandise.getCards(color);
        if (cards.size() > 0) {
            int price = merchandise.getPrice(color);
            price += getAchievementTokenCount(color);
            int score = price * cards.size();
            score = roundUpToNearestFive(score);
            int victoryPoints = score / 5;

            Collections.sort(cards, new Comparator<Card>() {
                @Override
                public int compare(Card o1, Card o2) {
                    return ((Integer) o1.getValue()).compareTo(o2.getValue());
                }
            });

//            System.out.println("*** ADDING " + victoryPoints + " " + color + " VPs to " + this.color);
            while (victoryPoints > 0) {
                if (!cards.isEmpty()) {
                    addVictoryPoint(cards.remove(0));
                } else {
                    addVictoryPoint(drawPile.draw());
                }
                victoryPoints--;
            }

            discardPile.addAll(cards);
            addAchievementToken(color);
        }

        merchandise.removeAllGoods(color);
    }

    public void discard(DiscardPile discardPile, List<Card> cards) {
        hand.discard(discardPile, cards);
    }

    public enum Color {
        Pink,
        Brown,
        White,
        Tan
    }

    private int roundUpToNearestFive(int score) {
        return (int) Math.ceil(score / 5F) * 5;
    }

    public static class Merchandise {
        private List<Card> cards = new ArrayList<>(27);
        private List<Card> storedCards = new ArrayList<>(27);

        public List<Card> getCards() {
            return cards;
        }

        public void setCards(List<Card> cards) {
            this.cards = cards;
        }

        public List<Card> getCards(GoodColor goodColor) {
            List<Card> result = new ArrayList<>(cards.size());
            for (Card card : cards) {
                if (card.getColor() == goodColor) {
                    result.add(card);
                }
            }
            return result;
        }

        public void pirateGoods(GoodColor color, DiscardPile discardPile) {
            Iterator<Card> iterator = cards.iterator();
            while (iterator.hasNext()) {
                Card card = iterator.next();
                if (card.getColor() == color && !storedCards.contains(card)) {
                    discardPile.add(card);
                    iterator.remove();
                }
            }
        }

        public void removeAllGoods(GoodColor color) {
            Iterator<Card> iterator = cards.iterator();
            while (iterator.hasNext()) {
                Card card = iterator.next();
                if (card.getColor() == color) {
                    iterator.remove();
                }
            }
        }

        public int getPrice(GoodColor color) {
            List<Card> set = getCards(color);
            int price = 0;
            for (Card card : set) {
                if (card.getValue() > price) {
                    price = card.getValue();
                }
            }
            return price;
        }
    }
}
