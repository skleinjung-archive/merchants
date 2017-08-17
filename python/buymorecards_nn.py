import random
import array
import buymorecards as bmc
from enum import Enum, unique
import bitstring as bstr


class TakeValidCardsEncoder:
    @staticmethod
    def encode_state(state):

        stream = bstr.BitStream()
        for cs in state:
            stream.append(bstr.pack('uint:3=v, uint:2=c, uint:3=s',
                                    v=cs.card.value - 1,
                                    c=cs.card.color.value - 1,
                                    s=cs.state.value - 1))

        return list(stream.bytes)

    @staticmethod
    def decode_result(state, result):
        return state[result].card.color, state[result].card.value

    @staticmethod
    def get_encoded_state_size():
        return 108
