import random
import array
import buymorecards as bmc
from enum import Enum, unique


class TakeValidCardsEncoder:
    @staticmethod
    def encode_state(state):
        result = []
        for card_state in state:
            result.extend([card_state.card.color.value, card_state.card.value, card_state.state.value])
        return result

    @staticmethod
    def decode_result(state, result):
        return state[result].card.color, state[result].card.value

    @staticmethod
    def get_encoded_state_size():
        return 108 * 3
