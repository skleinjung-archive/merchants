package com.thrashplay.merchants.controller;

import com.thrashplay.merchants.engine.Action;
import com.thrashplay.merchants.model.GameState;

import java.util.List;

/**
 * Copyright (c) 2016 Sean Kleinjung to present.
 * All rights reserved.
 */
public interface MerchantsPlayerController {
    Action selectAction(GameState gameState, List<Action> availableActions);
}
