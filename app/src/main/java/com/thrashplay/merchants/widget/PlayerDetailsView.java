package com.thrashplay.merchants.widget;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.thrashplay.merchants.R;
import com.thrashplay.merchants.model.Card;
import com.thrashplay.merchants.model.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Copyright (c) 2017 Sean Kleinjung
 * All rights reserved.
 */
public class PlayerDetailsView extends RelativeLayout {

//    @BindView(R.id.player_achievement_token_red)
//    TextView redAchievementTokens;
//    @BindView(R.id.player_achievement_token_yellow)
//    TextView yellowAchievementTokens;
//    @BindView(R.id.player_achievement_token_green)
//    TextView greenAchievementTokens;
//    @BindView(R.id.player_achievement_token_blue)
//    TextView blueAchievementTokens;

    @BindView(R.id.player_hand)
    CardRecyclerView handRecyclerView;

    @BindView(R.id.player_merchandise)
    CardRecyclerView merchandiseRecyclerView;

    // the player whose details are being displayed
    private Player player;

    // the player viewing this ... view
    private Player viewingPlayer;

    private CardRecyclerView.CardViewClickHandler cardViewClickHandler = new CardViewClickHandler();
    private List<HandSelectionListener> handSelectionListeners = new LinkedList<>();

    public PlayerDetailsView(Context context) {
        this(context, null);
    }

    public PlayerDetailsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerDetailsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_player_details, this);
        ButterKnife.bind(this);

        handRecyclerView.setSelectionEnabled(true);
        handRecyclerView.addCardViewClickHandler(cardViewClickHandler);
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
        updateUi();
    }

    public Player getViewingPlayer() {
        return viewingPlayer;
    }

    public void setViewingPlayer(Player viewingPlayer) {
        this.viewingPlayer = viewingPlayer;
        updateUi();
    }

    public List<Card> getHandSelection() {
        return handRecyclerView.getSelectedCards();
    }

    private void updateUi() {
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

        handRecyclerView.setShowCardBacksOnly(viewingPlayer != null && viewingPlayer != player);
        handRecyclerView.getAdapter().getItems().clear();
        handRecyclerView.getAdapter().getItems().addAll(player.getHand().getCards());
        handRecyclerView.getAdapter().notifyDataSetChanged();

        merchandiseRecyclerView.getAdapter().getItems().clear();
        merchandiseRecyclerView.getAdapter().getItems().addAll(player.getMerchandise().getCards());
        merchandiseRecyclerView.getAdapter().notifyDataSetChanged();

//        redAchievementTokens.setText(String.valueOf(player.getAchievementTokenCount(GoodColor.Red)));
//        yellowAchievementTokens.setText(String.valueOf(player.getAchievementTokenCount(GoodColor.Yellow)));
//        greenAchievementTokens.setText(String.valueOf(player.getAchievementTokenCount(GoodColor.Green)));
//        blueAchievementTokens.setText(String.valueOf(player.getAchievementTokenCount(GoodColor.Blue)));
    }

    public void addHandSelectionListener(HandSelectionListener listener) {
        if (listener != null) {
            handSelectionListeners.add(listener);
        }
    }

    public void removeHandSelectionListener(HandSelectionListener listener) {
        handSelectionListeners.remove(listener);
    }

    private void fireOnHandSelectionChanged(List<Card> selectedCards) {
        for (HandSelectionListener listener : handSelectionListeners) {
            listener.onHandSelectionChanged(selectedCards);
        }
    }

    public interface HandSelectionListener {
        void onHandSelectionChanged(List<Card> selectedCards);
    }

    private class CardViewClickHandler implements CardRecyclerView.CardViewClickHandler {
        @Override
        public void onCardClicked(int adapterPosition, CardView cardView) {
            if (handSelectionListeners.isEmpty()) {
                // do nothing if nobody is listening
                return;
            }

            List<Card> selectedCards = new ArrayList<>(handRecyclerView.getAdapter().getItemCount());
            for (int i = 0; i < handRecyclerView.getAdapter().getItemCount(); i++) {
                Card card = handRecyclerView.getAdapter().getItem(i);
                if (card.isSelected()) {
                    selectedCards.add(card);
                }
            }

            fireOnHandSelectionChanged(selectedCards);
        }
    }
}
