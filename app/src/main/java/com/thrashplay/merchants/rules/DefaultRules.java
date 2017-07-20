package com.thrashplay.merchants.rules;

/**
 * Copyright (c) 2016 Sean Kleinjung to present.
 * All rights reserved.
 */
public class DefaultRules implements Rules {
    @Override
    public int getMinPlayerCount() {
        return 2;
    }

    @Override
    public int getMaxPlayerCount() {
        return 4;
    }

    @Override
    public int getNumberOfTwosPerColor() {
        return 11;
    }

    @Override
    public int getNumberOfThreesPerColor() {
        return 9;
    }

    @Override
    public int getNumberOfFivesPerColor() {
        return 7;
    }

    @Override
    public int getStartingMarketSize() {
        return 5;
    }

    @Override
    public int getNumberOfCardsToAddWhenCreatingNewMarket() {
        return 2;
    }

    @Override
    public int getFarmSize() {
        return 3;
    }
}
