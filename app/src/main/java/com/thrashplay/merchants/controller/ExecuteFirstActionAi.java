package com.thrashplay.merchants.controller;

import com.thrashplay.merchants.engine.Action;
import com.thrashplay.merchants.model.GameState;

import java.util.List;

/**
 * Copyright (c) 2016 Sean Kleinjung to present.
 * All rights reserved.
 */
public class ExecuteFirstActionAi implements MerchantsPlayerController {
    @Override
    public Action selectAction(GameState gameState, List<Action> availableActions) {
        if (availableActions == null || availableActions.size() < 1) {
            throw new IllegalArgumentException("There are no available actions to take.");
        }

        return availableActions.get(0);
    }
}
