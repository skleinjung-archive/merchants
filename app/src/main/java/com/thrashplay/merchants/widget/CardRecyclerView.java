package com.thrashplay.merchants.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thrashplay.merchants.R;
import com.thrashplay.merchants.model.Card;
import com.thrashplay.merchants.util.Verify;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Copyright (c) 2017 Sean Kleinjung
 * All rights reserved.
 */
public class CardRecyclerView extends RecyclerView {

    private List<CardViewClickHandler> cardViewClickHandlers = new LinkedList<>();

    private CardSelectionHandler cardSelectionHandler;
    private boolean multiSelectEnabled = true;
    private boolean showCardBacksOnly;

    public CardRecyclerView(Context context) {
        this(context, null);
    }

    public CardRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        CardAdapter adapter = new CardRecyclerView.CardAdapter(new ArrayList<Card>(0));
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        setAdapter(adapter);
        setLayoutManager(layoutManager);

        setItemAnimator(null);

        final OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                fireOnCardViewClicked(getChildAdapterPosition(v), (CardView) ((ViewGroup) v).getChildAt(0));
            }
        };

        addOnChildAttachStateChangeListener(new OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                view.setOnClickListener(onClickListener);
            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                view.setOnClickListener(null);
            }
        });
    }

    @Override
    public CardAdapter getAdapter() {
        return (CardAdapter) super.getAdapter();
    }

    public boolean isShowCardBacksOnly() {
        return showCardBacksOnly;
    }

    public void setShowCardBacksOnly(boolean showCardBacksOnly) {
        this.showCardBacksOnly = showCardBacksOnly;
        getAdapter().notifyDataSetChanged();
    }

    public boolean isSelectionEnabled() {
        return cardSelectionHandler != null;
    }

    public void setSelectionEnabled(boolean selectionEnabled) {
        if (selectionEnabled) {
            cardSelectionHandler = new CardSelectionHandler();
            addCardViewClickHandler(cardSelectionHandler);
        } else {
            removeCardViewClickHandler(cardSelectionHandler);
            cardSelectionHandler = null;
        }
    }

    public boolean isMultiSelectEnabled() {
        return multiSelectEnabled;
    }

    public void setMultiSelectEnabled(boolean multiSelectEnabled) {
        this.multiSelectEnabled = multiSelectEnabled;
    }

    public List<Card> getSelectedCards() {
        List<Card> result = new ArrayList<>(getAdapter().getItemCount());
        for (Card card : getAdapter().getItems()) {
            if (card.isSelected()) {
                result.add(card);
            }
        }
        return result;
    }

    public void deselectAll() {
        for (int i = 0; i < getAdapter().getItemCount(); i++) {
            Card card = getAdapter().getItem(i);
            if (card.isSelected()) {
                card.setSelected(false);
                getAdapter().notifyItemChanged(i);
            }
        }
    }

    public void addCardViewClickHandler(CardViewClickHandler handler) {
        cardViewClickHandlers.add(handler);
    }

    public void removeCardViewClickHandler(CardViewClickHandler handler) {
        cardViewClickHandlers.remove(handler);
    }

    private void fireOnCardViewClicked(int adapterPosition, CardView v) {
        for (CardViewClickHandler handler : cardViewClickHandlers) {
            handler.onCardClicked(adapterPosition, v);
        }
    }

    public interface CardViewClickHandler {
        void onCardClicked(int adapterPosition, CardView cardView);
    }

    private final class CardSelectionHandler implements CardViewClickHandler {
        @Override
        public void onCardClicked(int adapterPosition, CardView cardView) {
            // if no multi-select, deselect all other selected cards
            if (!multiSelectEnabled) {
                for (int i = 0; i < getAdapter().getItemCount(); i++) {
                    if (i == adapterPosition) {
                        continue;
                    }

                    Card card = getAdapter().getItem(i);
                    if (card.isSelected()) {
                        card.setSelected(false);
                        getAdapter().notifyItemChanged(i);
                    }
                }
            }

            // select the card that was clicked
            cardView.getCard().setSelected(!cardView.getCard().isSelected());
            getAdapter().notifyItemChanged(adapterPosition);
        }
    }

    public class CardViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.card)
        CardView cardView;

        public CardViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public CardView getCardView() {
            return cardView;
        }

        public void bind(Card card) {
            cardView.setCard(card);
            cardView.setSelected(card.isSelected());
            cardView.setShowBackOnly(showCardBacksOnly);
        }
    }

    public class CardAdapter extends RecyclerView.Adapter<CardViewHolder> {
        private List<Card> items;

        public CardAdapter(List<Card> items) {
            Verify.notNull("items", items);
            this.items = items;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public List<Card> getItems() {
            return items;
        }

        public Card getItem(int position) {
            return getItems().get(position);
        }

        @Override
        public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View cardView = inflater.inflate(R.layout.item_card, parent, false);
            return new CardViewHolder(cardView);
        }

        @Override
        public void onBindViewHolder(CardViewHolder holder, int position) {
            holder.bind(getItem(position));
        }
    }
}
