package com.thrashplay.merchants.widget;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thrashplay.merchants.R;
import com.thrashplay.merchants.model.GoodColor;
import com.thrashplay.merchants.model.Player;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Copyright (c) 2017 Sean Kleinjung
 * All rights reserved.
 */
public class PlayerSummaryView extends RelativeLayout {

    @BindView(R.id.player_summary_name)
    TextView name;

    @BindView(R.id.player_achievement_tokens_container)
    ViewGroup tokensContainer;

    @BindView(R.id.player_achievement_token_red)
    TextView redAchievementToken;
    @BindView(R.id.player_achievement_token_yellow)
    TextView yellowAchievementToken;
    @BindView(R.id.player_achievement_token_green)
    TextView greenAchievementToken;
    @BindView(R.id.player_achievement_token_blue)
    TextView blueAchievementToken;

    @BindView(R.id.player_merchandise_container)
    ViewGroup merchandiseContainer;

    private Player player;

    public PlayerSummaryView(Context context) {
        this(context, null);
    }

    public PlayerSummaryView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerSummaryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_player_summary, this);
        ButterKnife.bind(this);
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
        updateUi();
    }

    private void updateUi() {
        name.setText(player.getColor().name());

        switch (player.getColor()) {
            case Brown:
                setBackgroundColor(ContextCompat.getColor(getContext(), R.color.player_brown));
                break;

            case Pink:
                setBackgroundColor(ContextCompat.getColor(getContext(), R.color.player_pink));
                break;

            case Tan:
                setBackgroundColor(ContextCompat.getColor(getContext(), R.color.player_tan));
                break;

            case White:
                setBackgroundColor(ContextCompat.getColor(getContext(), R.color.player_white));
                break;
        }

        redAchievementToken.setText(String.valueOf(player.getAchievementTokenCount(GoodColor.Red)));
        redAchievementToken.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_token_red));

        yellowAchievementToken.setText(String.valueOf(player.getAchievementTokenCount(GoodColor.Yellow)));
        yellowAchievementToken.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_token_yellow));

        greenAchievementToken.setText(String.valueOf(player.getAchievementTokenCount(GoodColor.Green)));
        greenAchievementToken.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_token_green));

        blueAchievementToken.setText(String.valueOf(player.getAchievementTokenCount(GoodColor.Blue)));
        blueAchievementToken.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_token_blue));

        int index = 0;
        for (GoodColor color : GoodColor.values()) {
            ViewGroup row = (ViewGroup) merchandiseContainer.getChildAt(index++);

            TextView priceView = (TextView) row.getChildAt(0);
            priceView.setText(String.valueOf(player.getMerchandise().getPrice(color)));
            switch (color) {
                case Red:
                    priceView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_token_red));
                    break;

                case Yellow:
                    priceView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_token_yellow));
                    break;

                case Green:
                    priceView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_token_green));
                    break;

                case Blue:
                    priceView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_token_blue));
                    break;
            }

            ((TextView) row.getChildAt(1)).setText(" x " + player.getMerchandise().getCards(color).size());
        }
    }
}