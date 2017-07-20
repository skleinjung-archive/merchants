package com.thrashplay.merchants.engine;

import com.thrashplay.merchants.model.GameState;

/**
 * Copyright (c) 2016 Sean Kleinjung to present.
 * All rights reserved.
 */
public interface Action {
    void apply(GameState gameState);
}
