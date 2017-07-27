import random
import array
from enum import Enum, unique


@unique
class Color(Enum):
    Red = 1
    Yellow = 2
    Green = 3
    Blue = 4

    def __str__(self):
        return self.name


class Card:
    def __init__(self, color, value):
        self.color = color
        self.value = value

    def __str__(self):
        return "{}{}".format(self.value, self.color.name[0])

    def __repr__(self):
        return "{}{}".format(self.value, self.color.name[0])

    def get_index(self):
        index = self._get_color_index_offset(self.color)
        if self.value == 3:
            index += 1
        if self.value == 5:
            index += 2
        return index

    @staticmethod
    def _get_color_index_offset(color):
        if color == Color.Red:
            return 0
        if color == Color.Yellow:
            return 3
        if color == Color.Green:
            return 6
        if color == Color.Blue:
            return 9
        return 10000


class Rules:
    def __init__(self):
        self.numberOfTwosPerColor = 11
        self.numberOfThreesPerColor = 9
        self.numberOfFivesPerColor = 7

        self.marketSize = 5

        self.minimumStartingHandValue = 8


class Player:
    def __init__(self, name):
        self.name = name
        self.hand = []
        self.goods = []

    def draw_card(self, deck):
        self.hand.append(deck.pop(0))

    def discard_card(self, card):
        self.hand.remove(card)

    def buy_cards(self, cards):
        self.goods.extend(cards)


class BaseModel:
    def __init__(self, rules):
        self.agentPlayer = Player("Agent")
        self.market = []
        self._deck = []
        self._rules = rules

        self._build_deck()

        self._initialize_hand(self.agentPlayer)
        self._refill_market()

        # print("Deck Size: {}".format(len(self._deck)))

    def get_deck_size(self):
        return len(self._deck)

    def take_card(self, player, card):
        if card not in self.market:
            raise ValueError("Card does not exist in market: {}".format(card))

        self.market.remove(card)
        player.hand.append(card)
        self._refill_market()

    def get_card_from_market(self, color, value):
        for card in self.market:
            if card.color == color and card.value == value:
                return card
        return None

    def is_game_over(self):
        return len(self.market) == 0 and len(self._deck) == 0

    def _initialize_hand(self, player):
        while sum(card.value for card in player.hand) < self._rules.minimumStartingHandValue:
            player.draw_card(self._deck)

    def _build_deck(self):
        for color in list(Color):
            for i in range(self._rules.numberOfTwosPerColor):
                self._deck.append(Card(color, 2))

            for i in range(self._rules.numberOfThreesPerColor):
                self._deck.append(Card(color, 3))

            for i in range(self._rules.numberOfFivesPerColor):
                self._deck.append(Card(color, 5))

        random.shuffle(self._deck)

    def _refill_market(self):
        while len(self.market) < 5:
            if len(self._deck) < 1:
                break
            self.market.append(self._deck.pop(0))


class TakeValidCardsModel(BaseModel):
    def __init__(self, *args, **kwargs):
        super(TakeValidCardsModel, self).__init__(*args, **kwargs)


class TakeMoreCardsModel(BaseModel):
    def __init__(self, *args, **kwargs):
        super(TakeMoreCardsModel, self).__init__(*args, **kwargs)
        self.adversaryPlayer = Player("Adversary")
        self._initialize_hand(self.adversaryPlayer)


class TakeMoreCardsAdversary:
    def get_card_to_take(self, takeMoreCardsModel):
        NotImplementedError("Abstract implementation of TakeMoreCardsAdversary")
        # return card


class PickFirstAvailableTakeMoreCardsAdversary:
    def get_card_to_take(self, take_more_cards_model):
        if len(take_more_cards_model.market) > 0:
            return take_more_cards_model.market[0]
        else:
            return None


class TakeValidCardsEnvironment:
    def __init__(self):
        self._model = None

    def reset(self):
        self._model = TakeValidCardsModel(Rules())
        return self.get_state()

    def step(self, color, value):
        card = self._get_card_from_market(Card(color, value))

        if card is None:
            reward = -10
            done = True
        else:
            self._model.take_card(self._model.agentPlayer, card)
            done = self._model.is_game_over()
            if done:
                reward = sum(card.value for card in self._model.agentPlayer.hand)
            else:
                reward = 1

        return self.get_state(), reward, done

    def get_state(self):
        #result = [self._bmc.get_deck_size(), len(self._bmc.adversaryPlayer.hand)]
        result = []
        result.extend(self._get_card_counts(self._model.market))
        # result.extend(self._get_card_counts(self._bmc.adversaryPlayer.goods))
        # result.extend(self._get_card_counts(self._bmc.agentPlayer.goods))
        # result.extend(self._get_card_counts(self._bmc.agentPlayer.hand))
        return result

    def _get_card_counts(self, cards):
        result = array.array('i', (0 for i in range(0, 12)))
        for card in cards:
            result[card.get_index()] += 1
        return result

    def _get_card_from_market(self, card):
        for marketCard in self._model.market:
            if marketCard.color == card.color and marketCard.value == card.value:
                return marketCard

        return None


class TakeMoreCardsEnvironment:
    def __init__(self):
        self._model = None
        self._adversary = PickFirstAvailableTakeMoreCardsAdversary()

    def reset(self):
        self._model = TakeMoreCardsModel(Rules())
        return self.get_state()

    def step(self, color, value):
        card = self._model.get_card_from_market(color, value)

        if card is None:
            reward = -10
            done = True
        else:
            self._model.take_card(self._model.agentPlayer, card)

            done = self._model.is_game_over()
            if not done:
                self._model.take_card(self._model.adversaryPlayer, self._adversary.get_card_to_take(self._model))

            done = self._model.is_game_over()
            if done:
                reward = sum(card.value for card in self._model.agentPlayer.hand)
            else:
                reward = 1

        return self.get_state(), reward, done

    def render(self):
        print("Market: {}, Agent: {}, Adversary: {}".format(self._model.market, self._model.agentPlayer.hand, self._model.adversaryPlayer.hand))

    def get_state(self):
        #result = [self._bmc.get_deck_size(), len(self._bmc.adversaryPlayer.hand)]
        result = []
        result.extend(self._get_card_counts(self._model.market))
        # result.extend(self._get_card_counts(self._bmc.adversaryPlayer.goods))
        # result.extend(self._get_card_counts(self._bmc.agentPlayer.goods))
        # result.extend(self._get_card_counts(self._bmc.agentPlayer.hand))
        return result

    def _get_card_counts(self, cards):
        result = array.array('i', (0 for i in range(0, 12)))
        for card in cards:
            result[card.get_index()] += 1
        return result


@unique
class Environment(Enum):
    TakeValidCards = 1
    TakeMoreCards = 2
    BuyMoreCards = 3

    def __str__(self):
        return self.name


def get_environment(which):
    if which == Environment.TakeValidCards:
        return TakeValidCardsEnvironment()
    if which == Environment.TakeMoreCards:
        return TakeMoreCardsEnvironment()
    if which == Environment.BuyMoreCards:
        raise NotImplementedError("BuyMoreCards environment not implemented")

    raise ValueError("Unknown environment: " + which)
