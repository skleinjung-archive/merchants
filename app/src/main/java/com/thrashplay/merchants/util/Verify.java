package com.thrashplay.merchants.util;

/**
 * Copyright (c) 2017 Sean Kleinjung
 * All rights reserved.
 */
public class Verify {
    public static void notNull(Object parameter) {
        notNull("parameter", parameter);
    }

    public static void notNull(String name, Object parameter) {
        if (parameter == null) {
            throw new NullPointerException(String.format("%s cannot be null", name));
        }
    }
}
