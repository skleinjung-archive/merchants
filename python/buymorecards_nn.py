import random
import array
import buymorecards as bmc
from enum import Enum, unique


class TakeValidCardsEncoder:
    def encode_state(self, state):
        return []

    def decode_result(self, state, result):
        return 1, 1, 1
