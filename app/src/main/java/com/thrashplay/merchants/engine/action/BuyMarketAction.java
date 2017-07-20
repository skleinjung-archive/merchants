package com.thrashplay.merchants.engine.action;

import com.thrashplay.merchants.engine.Action;
import com.thrashplay.merchants.model.Card;
import com.thrashplay.merchants.model.GameState;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2016 Sean Kleinjung to present.
 * All rights reserved.
 */
public class BuyMarketAction implements Action {
    private List<Card> cardsToSpend;

    public BuyMarketAction(List<Card> cardsToSpend) {
        this.cardsToSpend = cardsToSpend;
        Collections.sort(this.cardsToSpend);
    }

    @Override
    public void apply(GameState gameState) {
        gameState.buyMarket(cardsToSpend);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Buy Market [");
        for (int i = 0; i < cardsToSpend.size(); i++) {
            sb.append(cardsToSpend.get(i));
            if (i < cardsToSpend.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
