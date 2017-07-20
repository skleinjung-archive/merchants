package com.thrashplay.merchants.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.thrashplay.merchants.R;
import com.thrashplay.merchants.controller.BlockingPlayerController;
import com.thrashplay.merchants.controller.ExecuteRandomActionAi;
import com.thrashplay.merchants.engine.Action;
import com.thrashplay.merchants.engine.Engine;
import com.thrashplay.merchants.engine.action.FinishStoringGoodsAction;
import com.thrashplay.merchants.engine.action.TakeCoinAction;
import com.thrashplay.merchants.model.Card;
import com.thrashplay.merchants.model.GameState;
import com.thrashplay.merchants.model.GoodColor;
import com.thrashplay.merchants.model.Player;
import com.thrashplay.merchants.rules.DefaultRules;
import com.thrashplay.merchants.widget.CardRecyclerView;
import com.thrashplay.merchants.widget.CardView;
import com.thrashplay.merchants.widget.PlayerScoreView;
import com.thrashplay.merchants.widget.PlayerDetailsView;
import com.thrashplay.merchants.widget.PlayerSummaryView;
import com.thrashplay.merchants.widget.ShipLocationView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GameActivity extends AppCompatActivity {

    private Engine engine;
    private boolean executionPaused;

    @BindView(R.id.game_scores_grid)
    GridLayout scoresGrid;

    @BindView(R.id.game_draw_pile_size)
    TextView drawPileSize;

    @BindView(R.id.game_discard_pile_size)
    TextView discardPileSize;

    @BindView(R.id.game_farm)
    CardRecyclerView farmRecyclerView;
    CardRecyclerView.CardAdapter farmAdapter;

    @BindView(R.id.game_market)
    CardRecyclerView marketRecyclerView;
    CardRecyclerView.CardAdapter marketAdapter;

//    @BindView(R.id.player_status)
//    PlayerStatusView playerStatusView;

    @BindView(R.id.game_ship_locations_container)
    ViewGroup shipLocationsContainer;

    @BindView(R.id.game_player_score_view_1)
    PlayerScoreView playerScoreView1;
    @BindView(R.id.game_player_score_view_2)
    PlayerScoreView playerScoreView2;
    @BindView(R.id.game_player_score_view_3)
    PlayerScoreView playerScoreView3;
    @BindView(R.id.game_player_score_view_4)
    PlayerScoreView playerScoreView4;

    @BindView(R.id.game_player_section)
    ViewGroup playerSection;

    @BindView(R.id.game_player_summary_1)
    PlayerSummaryView playerSummaryView1;
    @BindView(R.id.game_player_summary_2)
    PlayerSummaryView playerSummaryView2;
    @BindView(R.id.game_player_summary_3)
    PlayerSummaryView playerSummaryView3;
    @BindView(R.id.game_player_summary_4)
    PlayerSummaryView playerSummaryView4;

    @BindView(R.id.game_player_details_1)
    PlayerDetailsView playerDetailsView1;
    @BindView(R.id.game_player_details_2)
    PlayerDetailsView playerDetailsView2;
    @BindView(R.id.game_player_details_3)
    PlayerDetailsView playerDetailsView3;
    @BindView(R.id.game_player_details_4)
    PlayerDetailsView playerDetailsView4;

    @BindView(R.id.game_action_radio_group)
    RadioGroup actions;
    @BindView(R.id.game_take_coin_button)
    Button takeCoin;
    @BindView(R.id.game_reserve_card_button)
    Button reserveCard;
    @BindView(R.id.game_buy_market_button)
    Button buyMarket;
    @BindView(R.id.game_finish_storing_goods_button)
    Button finishStoringGoods;

    @BindView(R.id.status_bar)
    TextView statusBar;

    BlockingPlayerController.BlockingPlayerControllerCallback playerInputCallback;

    private GameState gameState;
    private GameState.GameStateChangeListener gameStateChangeListener;

    Player playerBeingViewed;
    private PlayerDetailsView.HandSelectionListener handSelectionListener = new HandSelectionListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        ButterKnife.bind(this);

        playerScoreView1.setSelected(true);

        farmAdapter = farmRecyclerView.getAdapter();
        marketAdapter = marketRecyclerView.getAdapter();

        marketRecyclerView.setSelectionEnabled(true);
        marketRecyclerView.setMultiSelectEnabled(false);
        marketRecyclerView.addCardViewClickHandler(new MarketClickListener());

        farmRecyclerView.setSelectionEnabled(true);
        farmRecyclerView.setMultiSelectEnabled(false);
        farmRecyclerView.addCardViewClickHandler(new FarmClickListener());
    }

    @Override
    protected void onPause() {
        super.onPause();
        executionPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        executionPaused = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!executionPaused) {
                    engine = new Engine(
                            new DefaultRules(),
                            new BlockingPlayerController(new BlockingPlayerController.AsynchronousActionSelector() {
                                @Override
                                public void selectAction(List<Action> availableActions, BlockingPlayerController.BlockingPlayerControllerCallback callback) {
                                    prepareForUserInput(callback);
                                }
                            }),
                            new ExecuteRandomActionAi(),
                            new ExecuteRandomActionAi(),
                            new ExecuteRandomActionAi());
                    gameState = engine.getGameState();
                    gameStateChangeListener = new GameSateChangeListener();
                    gameState.addGameStateChangeListener(gameStateChangeListener);
                    playerBeingViewed = gameState.getPlayers().get(0);

                    gameStateChangeListener.onGameStateChanged(gameState);

                    while (engine.step() && !executionPaused) {
//                         do nothing
                        if (gameState.isInPirateRaid()) {
                            continue;
                        }

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                statusBar.setText(gameState.getActivePlayer().getColor() + "'s turn");
                            }
                        });

                        try {
//                            Thread.sleep(25);
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
//                             do nothing
                        }

//                        executionPaused = true;
                    }
                }
            }
        }).start();
    }

    private void prepareForUserInput(BlockingPlayerController.BlockingPlayerControllerCallback callback) {
        playerInputCallback = callback;

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (!gameState.isInPirateRaid()) {
                    takeCoin.setVisibility(View.VISIBLE);
                    reserveCard.setVisibility(View.VISIBLE);
                    buyMarket.setVisibility(View.VISIBLE);

                    finishStoringGoods.setVisibility(View.GONE);
                } else {
                    statusBar.setText("Pirates are raiding " + gameState.getColorBeingRaided());

                    takeCoin.setVisibility(View.GONE);
                    reserveCard.setVisibility(View.GONE);
                    buyMarket.setVisibility(View.GONE);

                    finishStoringGoods.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @OnClick(R.id.game_take_coin_button)
    public void onTakeCoinClicked() {
        if (!gameState.isInPirateRaid() && playerInputCallback != null) {
            playerInputCallback.selectAction(new TakeCoinAction(marketRecyclerView.getSelectedCards().get(0)));
            playerInputCallback = null;
        }

        takeCoin.setEnabled(false);
    }

    @OnClick(R.id.game_reserve_card_button)
    public void onReserveCardClicked() {
        reserveCard.setEnabled(false);
    }

    @OnClick(R.id.game_buy_market_button)
    public void onBuyMarketClicked() {
        /*
        if (!gameState.isInPirateRaid() && playerInputCallback != null) {
            playerInputCallback.selectAction(new BuyMarketAction(playerStatusView.getHandSelection()));
            playerInputCallback = null;
        }
        */
    }

    @OnClick(R.id.game_finish_storing_goods_button)
    public void onFinishStoringGoodsClicked() {
        if (gameState.isInPirateRaid() && playerInputCallback != null) {
            playerInputCallback.selectAction(new FinishStoringGoodsAction());
            playerInputCallback = null;
        }
    }

    @OnClick(R.id.game_player_summary_1)
    public void onPlayerSummary1Clicked() {
        expandPlayerDetails(R.id.game_player_details_1);
    }

    @OnClick(R.id.game_player_summary_2)
    public void onPlayerSummary2Clicked() {
        expandPlayerDetails(R.id.game_player_details_2);
    }

    @OnClick(R.id.game_player_summary_3)
    public void onPlayerSummary3Clicked() {
        expandPlayerDetails(R.id.game_player_details_3);
    }

    @OnClick(R.id.game_player_summary_4)
    public void onPlayerSummary4Clicked() {
        expandPlayerDetails(R.id.game_player_details_4);
    }

    private void expandPlayerDetails(int playerDetailsSectionId) {
        for (int i = 0; i < playerSection.getChildCount(); i++) {
            View child = playerSection.getChildAt(i);
            if (child.getId() == playerDetailsSectionId) {
                child.setVisibility(View.VISIBLE);
            } else if (child instanceof PlayerDetailsView) {
                child.setVisibility(View.GONE);
            }
        }
    }

    private class MarketClickListener implements CardRecyclerView.CardViewClickHandler {
        @Override
        public synchronized void onCardClicked(int adapterPosition, CardView cardView) {
            farmRecyclerView.deselectAll();
            updateActionButtonStatus();
        }
    }

    private class FarmClickListener implements CardRecyclerView.CardViewClickHandler {
        @Override
        public synchronized void onCardClicked(int adapterPosition, CardView cardView) {
            marketRecyclerView.deselectAll();
            updateActionButtonStatus();
        }
    }

    private class HandSelectionListener implements PlayerDetailsView.HandSelectionListener {
        @Override
        public void onHandSelectionChanged(List<Card> selectedCards) {
            updateActionButtonStatus();
        }
    }

    private void updateActionButtonStatus() {
        if ((marketRecyclerView.getSelectedCards().size() == 1 && farmRecyclerView.getSelectedCards().size() == 0)) {
            takeCoin.setEnabled(true);
        } else {
            takeCoin.setEnabled(false);
        }

        if ((marketRecyclerView.getSelectedCards().size() == 1 && farmRecyclerView.getSelectedCards().size() == 0)
                || (marketRecyclerView.getSelectedCards().size() == 0 && farmRecyclerView.getSelectedCards().size() == 1)) {
            reserveCard.setEnabled(true);
        } else {
            reserveCard.setEnabled(false);
        }

        /*
        int selectedHandValue = 0;
        for (Card card : playerStatusView.getHandSelection()) {
            selectedHandValue += card.getValue();
        }
        if (selectedHandValue >= gameState.getMarket().getCostToBuyFor(gameState.getActivePlayer())) {
            buyMarket.setEnabled(true);
        } else {
            buyMarket.setEnabled(false);
        }
        */
    }

    private class GameSateChangeListener implements GameState.GameStateChangeListener {
        @Override
        public void onGameStateChanged(final GameState gameState) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    updateScores(gameState);
                    updatePileSizes(gameState);
                    updateFarm(gameState);
                    updateMarket(gameState);
                    updateShipPositions(gameState);
//                    updatePlayerSelectionButtons(gameState);

                    updatePlayerSummaries(gameState);
                    updatePlayerDetails(gameState);

                    /*
                    playerStatusView.setPlayer(playerBeingViewed);
                    if (playerBeingViewed == gameState.getPlayers().get(0)) {
                        playerStatusView.addHandSelectionListener(handSelectionListener);
                    } else {
                        playerStatusView.removeHandSelectionListener(handSelectionListener);
                    }
                    */
                }
            });
        }

        private void updatePlayerSummaries(GameState gameState) {
            if (gameState.getPlayers().size() > 0) {
                playerSummaryView1.setPlayer(gameState.getPlayers().get(0));
                playerSummaryView1.setVisibility(View.VISIBLE);
            } else {
                playerSummaryView1.setVisibility(View.GONE);
            }

            if (gameState.getPlayers().size() > 1) {
                playerSummaryView2.setPlayer(gameState.getPlayers().get(1));
                playerSummaryView2.setVisibility(View.VISIBLE);
            } else {
                playerSummaryView2.setVisibility(View.GONE);
            }

            if (gameState.getPlayers().size() > 2) {
                playerSummaryView3.setPlayer(gameState.getPlayers().get(2));
                playerSummaryView3.setVisibility(View.VISIBLE);
            } else {
                playerSummaryView3.setVisibility(View.GONE);
            }

            if (gameState.getPlayers().size() > 3) {
                playerSummaryView4.setPlayer(gameState.getPlayers().get(3));
                playerSummaryView4.setVisibility(View.VISIBLE);
            } else {
                playerSummaryView4.setVisibility(View.GONE);
            }
        }

        private void updatePlayerDetails(GameState gameState) {
            if (gameState.getPlayers().size() > 0) {
                playerDetailsView1.setPlayer(gameState.getPlayers().get(0));
                // todo don't hardcode viewing player
                playerDetailsView1.setViewingPlayer(gameState.getPlayers().get(0));
            } else {
                playerDetailsView1.setVisibility(View.GONE);
            }

            if (gameState.getPlayers().size() > 1) {
                playerDetailsView2.setPlayer(gameState.getPlayers().get(1));
                playerDetailsView2.setViewingPlayer(gameState.getPlayers().get(0));
            } else {
                playerDetailsView2.setVisibility(View.GONE);
            }

            if (gameState.getPlayers().size() > 2) {
                playerDetailsView3.setPlayer(gameState.getPlayers().get(2));
                playerDetailsView3.setViewingPlayer(gameState.getPlayers().get(0));
            } else {
                playerDetailsView3.setVisibility(View.GONE);
            }

            if (gameState.getPlayers().size() > 3) {
                playerDetailsView4.setPlayer(gameState.getPlayers().get(3));
                playerDetailsView4.setViewingPlayer(gameState.getPlayers().get(0));
            } else {
                playerDetailsView4.setVisibility(View.GONE);
            }
        }

        private void updateScores(GameState gameState) {
            int childIndex = 0;
            for (Player player : gameState.getPlayers()) {
                PlayerScoreView playerScoreView = (PlayerScoreView) scoresGrid.getChildAt(childIndex++);
                playerScoreView.setPlayer(player);
//                playerScoreView.setSelected(player == gameState.getActivePlayer());
            }
        }

        private void updatePileSizes(GameState gameState) {
            drawPileSize.setText(String.valueOf(gameState.getDrawPile().size()));
            discardPileSize.setText(String.valueOf(gameState.getDiscardPile().getCards().size()));
        }

        private void updateFarm(GameState gameState) {
            farmAdapter.getItems().clear();
            farmAdapter.getItems().addAll(gameState.getFarm().getCards());
            farmAdapter.notifyDataSetChanged();
        }

        private void updateMarket(GameState gameState) {
            marketAdapter.getItems().clear();
            marketAdapter.getItems().addAll(gameState.getMarket().getCards());
            marketAdapter.notifyDataSetChanged();
        }

        private void updateShipPositions(GameState gameState) {
            List<List<GoodColor>> shipPositions = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                shipPositions.add(new ArrayList<GoodColor>(4));
            }

            for (GoodColor color : GoodColor.values()) {
                shipPositions.get(gameState.getShipPosition(color)).add(color);
            }

            for (int i = 0; i < 6; i++) {
                ((ShipLocationView) shipLocationsContainer.getChildAt(i)).setShipsHere(shipPositions.get(i));
            }
        }

        /*
        private void updatePlayerSelectionButtons(GameState gameState) {
            int childIndex = 0;
            for (Player player : gameState.getPlayers()) {
                Button playerSelectionButton = (Button) playerSelectionButtons.getChildAt(childIndex++);
                playerSelectionButton.setText(player.getColor().name());
                playerSelectionButton.setVisibility(View.VISIBLE);

                switch (player.getColor()) {
                    case Brown:
                        playerSelectionButton.setBackground(ContextCompat.getDrawable(GameActivity.this, R.drawable.background_player_brown));
                        break;

                    case Pink:
                        playerSelectionButton.setBackground(ContextCompat.getDrawable(GameActivity.this, R.drawable.background_player_pink));
                        break;

                    case Tan:
                        playerSelectionButton.setBackground(ContextCompat.getDrawable(GameActivity.this, R.drawable.background_player_tan));
                        break;

                    case White:
                        playerSelectionButton.setBackground(ContextCompat.getDrawable(GameActivity.this, R.drawable.background_player_white));
                        break;
                }
            }
        }
        */
    }
}
