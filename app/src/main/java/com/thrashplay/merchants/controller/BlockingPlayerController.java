package com.thrashplay.merchants.controller;

import com.thrashplay.merchants.engine.Action;
import com.thrashplay.merchants.model.GameState;

import java.util.List;

/**
 * Copyright (c) 2017 Sean Kleinjung
 * All rights reserved.
 */
public class BlockingPlayerController implements MerchantsPlayerController {
    private AsynchronousActionSelector actionSelector;

    public BlockingPlayerController(AsynchronousActionSelector actionSelector) {
        this.actionSelector = actionSelector;
    }

    @Override
    public Action selectAction(GameState gameState, List<Action> availableActions) {
        synchronized (this) {
            BlockingPlayerControllerCallback callback = new BlockingPlayerControllerCallback();
            actionSelector.selectAction(availableActions, callback);

            while (!callback.isReady()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // do nothing
                }
            }

            return callback.getSelectedAction();
        }
    }

    public interface AsynchronousActionSelector {
        void selectAction(List<Action> availableActions, BlockingPlayerControllerCallback callback);
    }

    public class BlockingPlayerControllerCallback {
        private Action selectedAction;

        public synchronized void selectAction(Action action) {
            synchronized (BlockingPlayerController.this) {
                selectedAction = action;
                BlockingPlayerController.this.notify();
            }
        }

        public Action getSelectedAction() {
            synchronized (BlockingPlayerController.this) {
                return selectedAction;
            }
        }

        public boolean isReady() {
            synchronized (BlockingPlayerController.this) {
                return selectedAction != null;
            }
        }
    }
}
