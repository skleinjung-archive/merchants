package com.thrashplay.merchants.engine.action;

import com.thrashplay.merchants.engine.Action;
import com.thrashplay.merchants.model.GameState;

/**
 * Copyright (c) 2017 Sean Kleinjung
 * All rights reserved.
 */
public class FinishStoringGoodsAction implements Action {
    @Override
    public void apply(GameState gameState) {
        gameState.finishStoringGoods();
    }

    @Override
    public String toString() {
        return "Finish storing goods";
    }
}
