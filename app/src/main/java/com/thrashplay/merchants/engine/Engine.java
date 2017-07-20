package com.thrashplay.merchants.engine;

import com.thrashplay.merchants.controller.MerchantsPlayerController;
import com.thrashplay.merchants.engine.action.BuyMarketAction;
import com.thrashplay.merchants.engine.action.FinishStoringGoodsAction;
import com.thrashplay.merchants.engine.action.TakeCoinAction;
import com.thrashplay.merchants.model.Card;
import com.thrashplay.merchants.model.GoodColor;
import com.thrashplay.merchants.model.GameState;
import com.thrashplay.merchants.model.Market;
import com.thrashplay.merchants.model.Player;
import com.thrashplay.merchants.rules.Rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Copyright (c) 2016 Sean Kleinjung to present.
 * All rights reserved.
 */
public class Engine {

    private static final int MAX_ACTION_OPTIONS = 23;

    private Rules rules;
    private GameState gameState;
    private Map<Player, MerchantsPlayerController> playerControllers = new HashMap<>();

    private List<ActionExecutionListener> actionExecutionListeners = new LinkedList<>();

    public Engine(Rules rules, MerchantsPlayerController... ais) {
        this.rules = rules;

        Player[] players = new Player[ais.length];
        List<Player.Color> playerColors = new ArrayList<>(Arrays.asList(Player.Color.values()));
        Collections.shuffle(playerColors);
        for (int i = 0; i < players.length; i++) {
            players[i] = new Player(playerColors.get(i));
            playerControllers.put(players[i], ais[i]);
        }

        this.gameState = new GameState(rules, players);
    }

    public void addActionExecutionListener(ActionExecutionListener listener) {
        actionExecutionListeners.add(listener);
    }

    public void removeActionExecutionListener(ActionExecutionListener listener) {
        actionExecutionListeners.remove(listener);
    }

    public GameState getGameState() {
        return gameState;
    }

    public boolean step() {
        MerchantsPlayerController ai = playerControllers.get(gameState.getActivePlayer());
        List<Action> availableActions = getAvailableActions();
        Action selectedAction = ai.selectAction(gameState, availableActions);
        execute(selectedAction);
        return !gameState.isGameOver();
    }

    public List<Action> getAvailableActions() {
        List<Action> result = new ArrayList<>(MAX_ACTION_OPTIONS);
        if (!gameState.isInPirateRaid()) {
            result.addAll(getAvailableCoinActions());
            result.addAll(getAvailablePurchaseActions());
            result.addAll(getAvailableReservationActions());
        } else {
            result.add(new FinishStoringGoodsAction());
        }

        fireOnAvailableActionsPrepared(result);

        return result;
    }

    public void execute(Action action) {
        fireBeforeActionExecuted(action);
        action.apply(gameState);
    }

    private Collection<? extends Action> getAvailableCoinActions() {
        List<Action> result = new ArrayList<>(getMaxMarketSize());
        for (Card card : gameState.getMarket().getCardsAvailableTo(gameState.getActivePlayer())) {
            result.add(new TakeCoinAction(card));
        }
        return result;
    }

    private Collection<? extends Action> getAvailablePurchaseActions() {
        List<List<Card>> spendOptions = getAllValidSpendCombinations(gameState.getMarket(), gameState.getActivePlayer());
        List<Action> result = new ArrayList<>(spendOptions.size());
        for (List<Card> cards : spendOptions) {
            result.add(new BuyMarketAction(cards));
        }
        return result;
    }

    private List<List<Card>> getAllValidSpendCombinations(Market market, Player player) {
        int costToBuy = market.getCostToBuyFor(player);
        List<List<Card>> result = new LinkedList<>();

        if (costToBuy > 0) {

            for (CombinationConfiguration config : getConfigurations(costToBuy)) {
                List<Card> twos = getCardsByValue(player.getHand().getCards(), 2);
                List<Card> threes = getCardsByValue(player.getHand().getCards(), 3);
                List<Card> fives = getCardsByValue(player.getHand().getCards(), 5);

                pruneDuplicatesOfColor(twos, config.twoCount);
                pruneDuplicatesOfColor(threes, config.threeCount);
                pruneDuplicatesOfColor(fives, config.fiveCount);

                getSpendCombinations(result, twos, threes, fives, config);
            }
        }

        return result;
    }

    private void pruneDuplicatesOfColor(List<Card> cards, int duplicatesAllowed) {
        Map<GoodColor, Integer> countsByColor = new HashMap<>();
        Iterator<Card> iterator = cards.iterator();
        while (iterator.hasNext()) {
            Card card = iterator.next();
            int count = 0;
            if (countsByColor.containsKey(card.getColor())) {
                count = countsByColor.get(card.getColor());
            }

            if (count >= duplicatesAllowed) {
                iterator.remove();
            } else {
                countsByColor.put(card.getColor(), ++count);
            }
        }
    }

    private List<Card> getCardsByValue(List<Card> cards, int value) {
        List<Card> result = new ArrayList<>(cards.size());
        for (Card card : cards) {
            if (card.getValue() == value) {
                result.add(card);
            }
        }
        return result;
    }

    private void getSpendCombinations(List<List<Card>> result, List<Card> twos, List<Card> threes, List<Card> fives, CombinationConfiguration config) {
        if (twos.size() < config.twoCount || threes.size() < config.threeCount || fives.size() < config.fiveCount) {
            return;
        }

        List<Set<List<Card>>> sets = new ArrayList<>(3);
        sets.add(getSubsets(twos, config.twoCount));
        sets.add(getSubsets(threes, config.threeCount));
        sets.add(getSubsets(fives, config.fiveCount));

        generateListCombinations(sets, result, new LinkedList<Card>(), 0);

    }

    private void generateListCombinations(List<Set<List<Card>>> lists, List<List<Card>> result, List<Card> current, int depth) {
        if (depth >= lists.size()) {
            result.add(current);
            return;
        }

        if (lists.get(depth).isEmpty()) {
            generateListCombinations(lists, result, current, depth + 1);
        } else {
            for (List<Card> cards : lists.get(depth)) {
                List<Card> copy = new LinkedList<>(current);
                copy.addAll(cards);
                generateListCombinations(lists, result, copy, depth + 1);
            }
        }
    }

/*&
        for (int i = 0; i < twosSets.size(); i++) {
            List<Card> option = new ArrayList<>(twos.size() + threes.size() + fives.size());
            List<Card> twosToUse = twosSets.get(i);
            option.addAll(twosToUse);

            for (int j = 0; j < threesSets.size(); j++) {
                List<Card> threesToUse = threesSets.get(j);
                option.addAll(threesToUse);

                for (int k = 0; k < fivesSets.size(); k++) {
                    List<Card> fivesToUse = fivesSets.get(k);
                    option.addAll(fivesToUse);
                }
            }

            result.add(option);
        }
    }
    */

    /*
    private void getSpendCombinations(List<List<Card>> result, List<Card> cards, int costToBuy) {
        getSpendCombinations_r(0, 0, result, new ArrayList<Card>(108), cards, costToBuy);

        List<Card> twos = new ArrayList<>(cards.size());
        List<Card> threes = new ArrayList<>(cards.size());
        List<Card> fives = new ArrayList<>(cards.size());
        for (Card card : cards) {
            if (card.getValue() == 2) {
                twos.add(card);
            } else if (card.getValue() == 3) {
                threes.add(card);
            } else if (card.getValue() == 5) {
                fives.add(card);
            }
        }

        CombinationConfiguration config = new CombinationConfiguration();
        config.twoCount = 3;

        getSpendCombinations(result, twos, threes, fives, config);

    }
    */


    public static void main(String[] args) {

        for (int i = 2; i <= 40; i++) {
            System.out.println(i + ": " + getConfigurations(i));
        }


/*

        List<Card> cards = new ArrayList<>();
        cards.add(new Card(GoodColor.Red, 2));
        cards.add(new Card(GoodColor.Green, 2));
        cards.add(new Card(GoodColor.Blue, 2));
        cards.add(new Card(GoodColor.Yellow, 2));

        System.out.println(getSubsets(cards, 4));

        */
    }

    private static List<CombinationConfiguration> getConfigurations(int targetValue) {
        List<CombinationConfiguration> result = new LinkedList<>();
        getConfigurations(targetValue, 5, new CombinationConfiguration(), result);
        return result;
    }

    private static void getConfigurations(int targetValue, int currentCardValue, CombinationConfiguration working, List<CombinationConfiguration> result) {
        switch (currentCardValue) {
            case 2:
                working.twoCount++;
                break;
            case 3:
                working.threeCount++;
                break;
            case 5:
                working.fiveCount++;
                break;
            default:
                throw new IllegalArgumentException("Illegal card value: " + currentCardValue);
        }

        if (working.getValue() >= targetValue) {
            result.add(new CombinationConfiguration(working));
        } else {
            getConfigurations(targetValue, currentCardValue, working, result);
        }

        int nextCardValue = 0;
        switch (currentCardValue) {
            case 2:
                working.twoCount--;
                break;
            case 3:
                working.threeCount--;
                nextCardValue = 2;
                break;
            case 5:
                working.fiveCount--;
                nextCardValue = 3;
                break;
            default:
                throw new IllegalArgumentException("Illegal card value: " + currentCardValue);
        }

        if (nextCardValue != 0) {
            getConfigurations(targetValue, nextCardValue, working, result);
        }

    }

    private static Set<List<Card>> getSubsets(List<Card> cards, int size) {
        if (size > cards.size()) {
            throw new IllegalArgumentException("size must be <= the length of the card list");
        }
        Set<List<Card>> result = new HashSet<>();
        getSubsets(cards, size, 0, new LinkedList<Card>(), result);
        return result;
    }

    private static void getSubsets(List<Card> superSet, int size, int index, LinkedList<Card> working, Set<List<Card>> result) {
        for (int i = index; i < superSet.size(); i++) {
            working.add(superSet.get(i));

            if (working.size() == size) {
                //noinspection unchecked
                result.add((List<Card>) working.clone());
            } else {
                getSubsets(superSet, size, i + 1, working, result);
            }

            working.remove(working.size() - 1);
        }
    }

    private static class CombinationConfiguration {
        int twoCount;
        int threeCount;
        int fiveCount;

        public CombinationConfiguration() {
        }

        public CombinationConfiguration(CombinationConfiguration toClone) {
            this(toClone.twoCount, toClone.threeCount, toClone.fiveCount);
        }

        public CombinationConfiguration(int twoCount, int threeCount, int fiveCount) {
            this.twoCount = twoCount;
            this.threeCount = threeCount;
            this.fiveCount = fiveCount;
        }

        public int getValue() {
            return 2 * twoCount + 3 * threeCount + 5 * fiveCount;
        }

        @Override
        public String toString() {
            return "(2x" + twoCount + " + 3x" + threeCount + " + 5x" + fiveCount + ")";
        }
    }

    /*
    private void getSpendCombinations_r(int index, int sumSoFar, List<List<Card>> result, ArrayList<Card> working, List<Card> cards, int costToBuy) {
        if (working.size() == 20) {
            return;
        }

        for (int i = index; i < cards.size(); i++) {
            working.add(cards.get(i));

            int currentSum = sumSoFar + cards.get(i).getValue();
            if (currentSum >= costToBuy) {
                //noinspection unchecked
                result.add((List<Card>) working.clone());
            } else {
                getSpendCombinations_r(i + 1, currentSum, result, working, cards, costToBuy);
            }

            working.remove(working.size() - 1);
        }
    }
    */

    private Collection<? extends Action> getAvailableReservationActions() {
        return new ArrayList<>(0);
    }

    private int getMaxMarketSize() {
        return rules.getFarmSize() + rules.getNumberOfCardsToAddWhenCreatingNewMarket() + (rules.getMaxPlayerCount() - 1);
    }

    private int getTotalCardValue(List<Card> cards) {
        int result = 0;
        for (Card card : cards) {
            result += card.getValue();
        }
        return result;
    }

    private void fireOnAvailableActionsPrepared(List<Action> actions) {
        for (ActionExecutionListener listener : actionExecutionListeners) {
            listener.onAvailableActionsPrepared(gameState, actions);
        }
    }

    private void fireBeforeActionExecuted(Action action) {
        for (ActionExecutionListener listener : actionExecutionListeners) {
            listener.beforeActionExecuted(gameState, action);
        }
    }

    public interface ActionExecutionListener {
        void onAvailableActionsPrepared(GameState gameState, List<Action> actions);
        void beforeActionExecuted(GameState gameState, Action action);
    }
}
