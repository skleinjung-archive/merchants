package com.thrashplay.merchants.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.thrashplay.merchants.R;
import com.thrashplay.merchants.model.GoodColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2017 Sean Kleinjung
 * All rights reserved.
 */
public class ShipLocationView extends View {

    private List<GoodColor> shipsHere = new ArrayList<>(0);

    private int shipMargin;
    private int borderSize;
    private Paint borderPaint;
    private Map<GoodColor, Paint> shipPaints;

    public ShipLocationView(Context context) {
        this(context, null);
    }

    public ShipLocationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShipLocationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        shipMargin = getResources().getDimensionPixelSize(R.dimen.game_ship_location_ship_margin);
        borderSize = getResources().getDimensionPixelSize(R.dimen.game_ship_location_border_width);

        borderPaint = new Paint();
        borderPaint.setStrokeWidth(borderSize);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);
        borderPaint.setColor(Color.BLACK);

        shipPaints = new HashMap<>();
        shipPaints.put(GoodColor.Red, createShipPaint(Color.RED));
        shipPaints.put(GoodColor.Blue, createShipPaint(Color.BLUE));
        shipPaints.put(GoodColor.Green, createShipPaint(Color.GREEN));
        shipPaints.put(GoodColor.Yellow, createShipPaint(Color.YELLOW));
    }

    private Paint createShipPaint(int color) {
        Paint paint = new Paint();
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);
        paint.setColor(color);
        return paint;
    }

    public List<GoodColor> getShipsHere() {
        return shipsHere;
    }

    public void setShipsHere(List<GoodColor> shipsHere) {
        this.shipsHere = shipsHere;
        if (shipsHere == null) {
            this.shipsHere = new ArrayList<>(0);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int shipSize = (getWidth() - (shipMargin * 4) - (borderSize * 2)) / 2;

        canvas.drawRect(0, 0, getWidth(), getHeight(), borderPaint);

        int position = 0;
        int x = 0;
        int y = 0;
        for (GoodColor color : shipsHere) {
            switch (position) {
                case 0:
                    x = shipMargin + borderSize;
                    y = shipMargin + borderSize;
                    break;

                case 1:
                    x = shipMargin * 2 + shipSize + borderSize;
                    y = shipMargin + borderSize;
                    break;

                case 2:
                    x = shipMargin + borderSize;
                    y = shipMargin * 2 + shipSize + borderSize;
                    break;

                case 3:
                    x = shipMargin * 2 + shipSize + borderSize;
                    y = shipMargin * 2 + shipSize + borderSize;
                    break;
            }

            canvas.drawRect(x, y, x + shipSize - 1, y + shipSize - 1, shipPaints.get(color));
            position++;
        }

    }
}

