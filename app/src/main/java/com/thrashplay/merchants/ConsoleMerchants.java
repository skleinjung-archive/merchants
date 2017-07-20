package com.thrashplay.merchants;

import com.thrashplay.merchants.controller.ExecuteRandomActionAi;
import com.thrashplay.merchants.engine.Engine;
import com.thrashplay.merchants.model.GameState;
import com.thrashplay.merchants.rules.DefaultRules;
import com.thrashplay.merchants.ui.text.TextBasedMerchantsUi;

/**
 * Copyright (c) 2016 Sean Kleinjung to present.
 * All rights reserved.
 */
public class ConsoleMerchants {
    public static void main(String[] args) {
        while (true) {
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println("================================================================");
            System.out.println("=========================== NEW GAME ===========================");
            System.out.println("================================================================");
            System.out.println();
            System.out.println();
            System.out.println();

            Engine engine = new Engine(new DefaultRules(), new ExecuteRandomActionAi(), new ExecuteRandomActionAi());
            GameState gameState = engine.getGameState();
            TextBasedMerchantsUi ui = new TextBasedMerchantsUi(gameState);
            gameState.addGameStateChangeListener(ui);
            engine.addActionExecutionListener(ui);

//            try {
                while (engine.step()) {
                    // do nothing
                }
//            } catch (IllegalStateException | IllegalArgumentException e) {
//                 do nothing
//                e.printStackTrace();
//            }
        }
    }
}
