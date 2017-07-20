package com.thrashplay.merchants.rules;

/**
 * Copyright (c) 2016 Sean Kleinjung to present.
 * All rights reserved.
 */
public interface Rules {
    // general rules
    int getMinPlayerCount();
    int getMaxPlayerCount();

    // deck composition rules
    int getNumberOfTwosPerColor();
    int getNumberOfThreesPerColor();
    int getNumberOfFivesPerColor();

    // market rules
    int getStartingMarketSize();
    int getNumberOfCardsToAddWhenCreatingNewMarket();

    // farm rules
    int getFarmSize();
}
