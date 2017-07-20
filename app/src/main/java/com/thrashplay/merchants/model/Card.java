package com.thrashplay.merchants.model;

/**
 * Copyright (c) 2016 Sean Kleinjung to present.
 * All rights reserved.
 */
public class Card implements Comparable<Card> {
    private GoodColor color;
    private int value;
    private int jars;
    private Player reservedBy;
    private boolean selected;

    public Card(GoodColor color, int value) {
        if (value != 2 && value != 3 && value != 5) {
            throw new IllegalArgumentException("Card value must be one of (2, 3, 5).");
        }

        this.color = color;
        this.value = value;
    }

    public GoodColor getColor() {
        return color;
    }

    public void setColor(GoodColor color) {
        this.color = color;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getJars() {
        return jars;
    }

    public void setJars(int jars) {
        this.jars = jars;
    }

    public boolean isReserved() {
        return getReservedBy() != null;
    }

    public boolean isAvailableTo(Player player) {
        return getReservedBy() == null || isReservedBy(player);
    }

    public boolean isReservedBy(Player player) {
        return getReservedBy() != null && getReservedBy().equals(player);
    }

    public Player getReservedBy() {
        return reservedBy;
    }

    void setReservedBy(Player reservedBy) {
        this.reservedBy = reservedBy;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public int compareTo(Card o) {
        int colorComparison = getColor().compareTo(o.getColor());
        return colorComparison != 0
                ? colorComparison
                : ((Integer) value).compareTo(o.getValue());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getValue());
        sb.append(getColorInitial());
        if (isReserved()) {
            sb.append("[");
            sb.append(getReservedBy().getColor().name());
            sb.append("]");
        }

        return sb.toString();
    }

    private char getColorInitial() {
        switch (getColor()) {
            case Blue:
                return 'B';
            case Green:
                return 'G';
            case Red:
                return 'R';
            case Yellow:
                return 'Y';
            default:
                return 'X';
        }
    }
}
