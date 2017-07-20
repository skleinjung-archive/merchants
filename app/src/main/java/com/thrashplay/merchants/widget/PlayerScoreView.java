package com.thrashplay.merchants.widget;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.TextView;

import com.thrashplay.merchants.R;
import com.thrashplay.merchants.model.Player;

/**
 * Copyright (c) 2017 Sean Kleinjung
 * All rights reserved.
 */
public class PlayerScoreView extends TextView {

    private Player player;

    public PlayerScoreView(Context context) {
        this(context, null);
    }

    public PlayerScoreView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerScoreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
        updateUi();
    }

    private void updateUi() {
        if (player == null) {
            setBackground(null);
            setText(null);
            return;
        }

        switch (player.getColor()) {
            case Brown:
                setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_player_brown));
                break;

            case Pink:
                setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_player_pink));
                break;

            case Tan:
                setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_player_tan));
                break;

            case White:
                setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_player_white));
                break;
        }

        setText(String.valueOf(player.getVictoryPointCount()));
    }
}
