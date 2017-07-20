package com.thrashplay.merchants.engine.action;

import com.thrashplay.merchants.engine.Action;
import com.thrashplay.merchants.model.Card;
import com.thrashplay.merchants.model.GameState;

/**
 * Copyright (c) 2016 Sean Kleinjung to present.
 * All rights reserved.
 */
public class TakeCoinAction implements Action {
    private Card coin;

    public TakeCoinAction(Card coin) {
        this.coin = coin;
    }

    @Override
    public void apply(GameState gameState) {
        gameState.takeCoin(coin);
    }

    @Override
    public String toString() {
        return "Take Coin [" + coin + "]";
    }
}
