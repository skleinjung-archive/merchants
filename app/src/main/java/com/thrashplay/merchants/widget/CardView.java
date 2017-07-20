package com.thrashplay.merchants.widget;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thrashplay.merchants.R;
import com.thrashplay.merchants.model.Card;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Copyright (c) 2017 Sean Kleinjung
 * All rights reserved.
 */
public class CardView extends RelativeLayout {

    @BindView(R.id.card_value)
    TextView value;

    private Card card;
    private boolean showBackOnly;

    public CardView(Context context) {
        this(context, null);
    }

    public CardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_card, this);
        ButterKnife.bind(this);
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
        updateUi();
    }

    public boolean isShowBackOnly() {
        return showBackOnly;
    }

    public void setShowBackOnly(boolean showBackOnly) {
        this.showBackOnly = showBackOnly;
        updateUi();
    }

    @Override
    public boolean isSelected() {
        return card.isSelected();
    }

    @Override
    public void setSelected(boolean selected) {
        card.setSelected(selected);
        super.setSelected(selected);
    }

    private void updateUi() {
        if (card == null) {
            setBackground(null);
            value.setText("");
            return;
        }

        if (showBackOnly) {
            setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_card_back));
            value.setText("");
        } else {
            switch (card.getColor()) {
                case Red:
                    setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_card_red));
                    break;

                case Yellow:
                    setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_card_yellow));
                    break;

                case Green:
                    setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_card_green));
                    break;

                case Blue:
                    setBackground(ContextCompat.getDrawable(getContext(), R.drawable.background_card_blue));
                    break;
            }

            value.setText(String.valueOf(card.getValue()));
        }
    }
}
