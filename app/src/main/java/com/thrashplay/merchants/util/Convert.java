package com.thrashplay.merchants.util;

import android.content.res.Resources;

/**
 * Copyright (c) 2017 Sean Kleinjung
 * All rights reserved.
 */
public final class Convert {
    /**
     * Convert a DP value to pixels using the current screen's density.
     */
    public static int dpsToPixels(Resources resources, float dps) {
        final float scale = resources.getDisplayMetrics().density;
        return (int) (dps * scale + 0.5f);
    }
}
