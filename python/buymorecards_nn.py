import random
import array
import buymorecards as bmc
from enum import Enum, unique
import bitstring as bstr


class TakeValidCardsEncoder:
    def encode_state(self, state):

        stream = bstr.BitStream()
        for cs in state:
            stream.append(bstr.pack('uint:3=v, uint:2=c, uint:3=s',
                                    v=cs.card.value - 1,
                                    c=cs.card.color.value - 1,
                                    s=cs.state.value - 1))
        return stream.bytes

    def decode_result(self, state, result):
        return 1, 1, 1
