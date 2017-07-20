package com.thrashplay.merchants.model;

import com.thrashplay.merchants.rules.Rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2016 Sean Kleinjung to present.
 * All rights reserved.
 */
public class GameState {
    private Rules rules;
    private List<Player> players = new ArrayList<>(4);
    private int currentPlayerIndex;
    private DiscardPile discardPile = new DiscardPile();
    private DrawPile drawPile = new DrawPile(discardPile);
    private Map<GoodColor, Integer> shipPositions;
    private Farm farm = new Farm();
    private Market market = new Market();
    private List<PirateRaid> pirateRaids = new ArrayList<>(4);

    private List<GameStateChangeListener> gameStateChangeListeners = new LinkedList<>();

    public GameState(Rules rules, Player... players) {
        if (players == null || players.length < rules.getMinPlayerCount()) {
            throw new IllegalArgumentException("Must supply at least two players");
        }
        if (players.length > rules.getMaxPlayerCount()) {
            throw new IllegalArgumentException("Must supply at no more than four players");
        }

        this.rules = rules;
        this.players.addAll(Arrays.asList(players));
        this.currentPlayerIndex = 0;

        initializeDrawPile();
        initializeMarket();
        initializeFarm();
        initializeStartingHands();
        initializeShipPositions();
    }

    private void initializeShipPositions() {
        shipPositions = new HashMap<>();
        for (GoodColor color : GoodColor.values()) {
            shipPositions.put(color, 0);
        }
    }

    private void initializeStartingHands() {
        for (int i = 0; i < players.size(); i++) {
            Player currentPlayer = players.get(i);
            while (getTotalValue(currentPlayer.getHand().getCards()) < 8) {
                drawPile.drawTo(currentPlayer.getHand());
            }
        }
    }

    private void initializeDrawPile() {
        for (GoodColor goodColor : GoodColor.values()) {
            for (int i = 0; i < rules.getNumberOfTwosPerColor(); i++) {
                drawPile.add(new Card(goodColor, 2));
            }
            for (int i = 0; i < rules.getNumberOfThreesPerColor(); i++) {
                drawPile.add(new Card(goodColor, 3));
            }
            for (int i = 0; i < rules.getNumberOfFivesPerColor(); i++) {
                drawPile.add(new Card(goodColor, 5));
            }
        }

        drawPile.shuffle();
    }

    private void initializeMarket() {
        for (int i = 0; i < rules.getStartingMarketSize(); i++) {
            market.add(drawPile.draw());
        }
    }

    private void initializeFarm() {
        for (int i = 0; i < rules.getFarmSize(); i++) {
            farm.add(drawPile.draw());
        }
    }

    public void addGameStateChangeListener(GameStateChangeListener listener) {
        gameStateChangeListeners.add(listener);
    }

    public void removeGameStateChangeListener(GameStateChangeListener listener) {
        gameStateChangeListeners.remove(listener);
    }

    public boolean isGameOver() {
        for (Player player : players) {
            int achievementTokens = 0;
            for (GoodColor color : GoodColor.values()) {
                achievementTokens += player.getAchievementTokenCount(color);
            }

            if (achievementTokens >= 8) {
                return true;
            }
        }

        return false;
    }

    private void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();

        if (market.getCardsAvailableTo(getCurrentPlayer()).size() == 0) {
            refreshMarket();
        }

        assertCorrectNumberOfCardsExist();
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public Player getActivePlayer() {
        if (isInPirateRaid()) {
            return getCurrentPirateRaid().getActivePlayer();
        } else {
            return getCurrentPlayer();
        }
    }

    public DrawPile getDrawPile() {
        return drawPile;
    }

    public DiscardPile getDiscardPile() {
        return discardPile;
    }

    public PirateRaid getCurrentPirateRaid() {
        return isInPirateRaid() ? pirateRaids.get(0) : null;
    }

    public boolean isInPirateRaid() {
        return pirateRaids != null && pirateRaids.size() > 0;
    }

    public GoodColor getColorBeingRaided() {
        if (!isInPirateRaid()) {
            return null;
        }
        return getCurrentPirateRaid().getColorBeingRaided();
    }

    public void takeCoin(Card card) {
        if (!getMarket().getCards().contains(card)) {
            throw new IllegalArgumentException("The specified coin is not in the market.");
        }
        if (!card.isAvailableTo(getCurrentPlayer())) {
            throw new IllegalArgumentException("The specified coin is reserved by another player.");
        }

        card.setSelected(false);
        card.setReservedBy(null);

        getMarket().getCards().remove(card);
        getCurrentPlayer().getHand().getCards().add(card);
        nextTurn();

        fireGameStateChanged();
    }

    public void buyMarket(List<Card> cardsToSpend) {
        Player currentPlayer = getCurrentPlayer();
        int marketValue = market.getCostToBuyFor(currentPlayer);
        int spendValue = getTotalValue(cardsToSpend);

        if (spendValue < marketValue) {
            throw new IllegalArgumentException("Must spend coins with a value greater than or equal to the available card value in the market.");
        }
        for (Card card : cardsToSpend) {
            if (!currentPlayer.getHand().getCards().contains(card)) {
                throw new IllegalArgumentException("Card '" + card + "' is not in the current player's hand.");
            }
        }

        List<Card> cardsPurchased = currentPlayer.purchase(discardPile, market, cardsToSpend);
        refreshMarket();
        advanceShips(cardsPurchased);

        nextTurn();

        fireGameStateChanged();
    }

    public void storeGoods(Card storageCard, List<Card> cardsToStore) {
        if (!isInPirateRaid()) {
            throw new IllegalStateException("Can only store goods during a pirate raid.");
        }
    }

    public void finishStoringGoods() {
        if (!isInPirateRaid()) {
            throw new IllegalStateException("Can only finish storing goods during a pirate raid.");
        }

        PirateRaid pirateRaid = getCurrentPirateRaid();
        pirateRaid.setActivePlayerDoneStoringGoods();
        if (pirateRaid.areAllPlayersDoneStoring()) {
            removePiratedGoods(pirateRaid.getColorBeingRaided());
            pirateRaids.remove(pirateRaid);
        }

        fireGameStateChanged();
    }

    private void advanceShips(List<Card> cardsPurchased) {
        int redAdvancement = 0;
        int yellowAdvancement = 0;
        int greenAdvancement = 0;
        int blueAdvancement = 0;
        for (int i = 0; i < cardsPurchased.size(); i++) {
            switch (cardsPurchased.get(i).getColor()) {
                case Red:
                    redAdvancement = Math.min(redAdvancement + 1, 2);
                    break;

                case Yellow:
                    yellowAdvancement = Math.min(yellowAdvancement + 1, 2);
                    break;

                case Green:
                    greenAdvancement = Math.min(greenAdvancement + 1, 2);
                    break;

                case Blue:
                    blueAdvancement = Math.min(blueAdvancement + 1, 2);
                    break;
            }
        }

        shipPositions.put(GoodColor.Red, Math.min(shipPositions.get(GoodColor.Red) + redAdvancement, 5));
        shipPositions.put(GoodColor.Yellow, Math.min(shipPositions.get(GoodColor.Yellow) + yellowAdvancement, 5));
        shipPositions.put(GoodColor.Green, Math.min(shipPositions.get(GoodColor.Green) + greenAdvancement, 5));
        shipPositions.put(GoodColor.Blue, Math.min(shipPositions.get(GoodColor.Blue) + blueAdvancement, 5));

        for (GoodColor color : GoodColor.values()) {
            if (shipPositions.get(color) == 5) {
                payday(color);
            }
        }
    }

    private void payday(GoodColor color) {
        shipPositions.put(color, 0);

        for (Player player : players) {
            player.score(discardPile, drawPile, color);
        }

        for (GoodColor colorToPirate : GoodColor.values()) {
            if (shipPositions.get(colorToPirate) == 4 || shipPositions.get(colorToPirate) == 3) {
                pirate(colorToPirate);
            }
        }
    }

    private void pirate(GoodColor color) {
        pirateRaids.add(new PirateRaid(color));
        shipPositions.put(color, 2);
    }

    private void removePiratedGoods(GoodColor color) {
        for (Player player : players) {
            player.getMerchandise().pirateGoods(color, discardPile);
        }
    }

    public Farm getFarm() {
        return farm;
    }

    public Market getMarket() {
        return market;
    }

    public int getShipPosition(GoodColor color) {
        return shipPositions.get(color);
    }

    private void refreshMarket() {
        farm.moveTo(market);

        for (int i = 0; i < rules.getNumberOfCardsToAddWhenCreatingNewMarket(); i++) {
            market.add(drawPile.draw());
        }

        for (int i = 0; i < rules.getFarmSize(); i++) {
            farm.add(drawPile.draw());
        }
    }

    private int getTotalValue(List<Card> cards) {
        int value = 0;
        for (Card card : cards) {
            value += card.getValue();
        }
        return value;
    }

    private void assertCorrectNumberOfCardsExist() {
        int cardCount = drawPile.size();
        cardCount += discardPile.getCards().size();
        cardCount += market.getCards().size();
        cardCount += farm.getCards().size();

        for (Player player : players) {
            cardCount += player.getHand().getCards().size();
            cardCount += player.getMerchandise().getCards().size();
            cardCount += player.getVictoryPointCount();
        }

        if (cardCount != 108) {
            throw new IllegalStateException("Inconsistent number of cards exist: " + cardCount);
        }
    }


    private void fireGameStateChanged() {
        for (GameStateChangeListener listener : gameStateChangeListeners) {
            listener.onGameStateChanged(this);
        }
    }

    public interface GameStateChangeListener {
        void onGameStateChanged(GameState gameState);
    }

    public class PirateRaid {
        private GoodColor colorBeingRaided;
        private int activePlayerIndex;
        private Map<Player, Boolean> playerFinishedStoringMap;

        public PirateRaid(GoodColor colorBeingRaided) {
            this.colorBeingRaided = colorBeingRaided;
            this.activePlayerIndex = currentPlayerIndex;
            playerFinishedStoringMap = new HashMap<>();
            for (Player player : players) {
                playerFinishedStoringMap.put(player, false);
            }
        }

        // returns the player currently storing goods
        public Player getActivePlayer() {
            return players.get(activePlayerIndex);
        }

        public void setActivePlayerDoneStoringGoods() {
            playerFinishedStoringMap.put(getActivePlayer(), true);
            activePlayerIndex = (activePlayerIndex + 1) % players.size();
        }

        public GoodColor getColorBeingRaided() {
            return colorBeingRaided;
        }

        public boolean areAllPlayersDoneStoring() {
            int doneCount = 0;
            for (Boolean done : playerFinishedStoringMap.values()) {
                if (done) {
                    doneCount++;
                }
            }

            return doneCount == players.size();
        }
    }
}
