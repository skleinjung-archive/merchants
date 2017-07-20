package com.thrashplay.merchants.ui.text;

import com.thrashplay.merchants.engine.Action;
import com.thrashplay.merchants.engine.Engine;
import com.thrashplay.merchants.engine.action.BuyMarketAction;
import com.thrashplay.merchants.model.Card;
import com.thrashplay.merchants.model.GameState;
import com.thrashplay.merchants.model.GoodColor;
import com.thrashplay.merchants.model.Player;
import com.thrashplay.merchants.ui.MerchantsUi;

import java.util.List;

/**
 * Copyright (c) 2016 Sean Kleinjung to present.
 * All rights reserved.
 */
public class TextBasedMerchantsUi implements MerchantsUi, GameState.GameStateChangeListener, Engine.ActionExecutionListener {
    private GameState gameState;

    public TextBasedMerchantsUi(GameState gameState) {
        this.gameState = gameState;
        printGameState();
    }

    @Override
    public void onGameStateChanged(GameState gameState) {
        printGameState();
    }

    @Override
    public void onAvailableActionsPrepared(GameState gameState, List<Action> actions) {
        System.out.print("Available Actions: ");
        boolean firstAction = true;
        int buyActionCount = 0;
        for (int i = 0; i < actions.size(); i++) {
            Action action = actions.get(i);
            if (action instanceof BuyMarketAction) {
                buyActionCount++;
            } else {
                if (!firstAction) {
                    System.out.print(", ");
                } else {
                    firstAction = false;
                }
                System.out.print(action);
            }
        }

        if (buyActionCount > 0) {
            if (!firstAction) {
                System.out.print(", ");
            }
            System.out.print("Buy Market x " + buyActionCount);
        }

        System.out.println();
    }

    @Override
    public void beforeActionExecuted(GameState gameState, Action action) {
        System.out.println();
        System.out.println("> " + action);
        System.out.println();
    }

    private void printGameState() {
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();

        System.out.println("------------------------------------------------------------------------------------------------------------------------------------------");
        if (gameState.isInPirateRaid()) {
            System.out.println("** Pirates are raiding " + gameState.getColorBeingRaided());
        }
        printDrawAndDiscardPiles();
        printScores();
        printFarm();
        printMarket();
        printShips();
        System.out.println("Active Player: " + gameState.getActivePlayer().getColor().name());
        System.out.println("------------------------------------------------------------------------------------------------------------------------------------------");

        for (Player player : gameState.getPlayers()) {
            System.out.println("        Color: " + player.getColor().name());
            printHand(player);
            printMerchandise(player);
            printAchievementTokens(player);
            System.out.println("------------------------------------------------------------------------------------------------------------------------------------------");
        }

    }

    private void printHand(Player player) {
        StringBuilder sb = new StringBuilder("         Hand: ");
        boolean firstCard = true;
        for (Card card : player.getHand().getCards()) {
            if (!firstCard) {
                sb.append(", ");
            } else {
                firstCard = false;
            }
            sb.append(card);
        }

        System.out.println(sb.toString());
    }

    private void printMerchandise(Player player) {
        StringBuilder sb = new StringBuilder("  Merchandise: ");
        for (GoodColor color : GoodColor.values()) {
            sb.append(color.name());
            sb.append(": [");

            boolean firstCard = true;
            for (Card card : player.getMerchandise().getCards(color)) {
                if (!firstCard) {
                    sb.append(", ");
                } else {
                    firstCard = false;
                }
                sb.append(card);
            }
            sb.append("] ");
        }

        System.out.println(sb.toString());
    }

    private void printAchievementTokens(Player player) {
        StringBuilder sb = new StringBuilder("  Ach. Tokens: ");
        boolean firstColor = true;
        for (GoodColor color : GoodColor.values()) {
            if (!firstColor) {
                sb.append(", ");
            } else {
                firstColor = false;
            }

            sb.append(color.name());
            sb.append(": ");
            sb.append(player.getAchievementTokenCount(color));
        }

        System.out.println(sb.toString());
    }

    private void printScores() {
        System.out.print("   VPs:");
        boolean firstPlayer = true;
        for (Player player : gameState.getPlayers()) {
            if (!firstPlayer) {
                System.out.print(",");
            } else {
                firstPlayer = false;
            }
            System.out.print(" " + player.getColor().name() + "[" + player.getVictoryPointCount() + "]");
        }
        System.out.println();
    }

    private void printDrawAndDiscardPiles() {
        System.out.println("  Draw: " + gameState.getDrawPile().size() + ", Discard: " + gameState.getDiscardPile().getCards().size());
    }


    private void printFarm() {
        System.out.print("  Farm:");
        for (Card card : gameState.getFarm().getCards()) {
            System.out.print(" " + card.toString());
        }
        System.out.println();
    }

    private void printMarket() {
        System.out.print("Market:");
        for (Card card : gameState.getMarket().getCards()) {
            System.out.print(" " + card.toString());
        }
        System.out.println();
    }

    private void printShips() {
        System.out.print(" Ships: ");

        for (int i = 0; i < 6; i++) {
            if (i == 5) {
                System.out.print("$");
            }  else if (i == 4 || i == 3) {
                System.out.print("*");
            }

            System.out.print("[");
            boolean firstShip = true;
            for (GoodColor color : GoodColor.values()) {
                if (gameState.getShipPosition(color) == i) {
                    if (!firstShip) {
                        System.out.print(", ");
                    } else {
                        firstShip = false;
                    }
                    System.out.print(color.name());
                }
            }
            System.out.print("]");
            if (i == 5) {
                System.out.print("$ ");
            }  else if (i == 4 || i == 3) {
                System.out.print("* ");
            } else {
                System.out.print(" ");
            }
        }

        System.out.println();
    }
}
