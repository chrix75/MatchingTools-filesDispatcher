package dispatcher.services

import domain.Address
import services.ObjectsPool

/**
 * Exemple: ADR/1//Rue De Dhuisy/0/0
 * Created by batman on 28/08/2016.
 */
class UnmatchcoderAddress implements Unmatchcoder<Address> {

    final ObjectsPool<Address> addressPool

    UnmatchcoderAddress() {
        addressPool = new ObjectsPool<>({ new Address() })
    }

    @Override
    Address unmatchcode(String field) {
        if (!field.startsWith("ADR/")) {
            throw new IllegalArgumentException("$field is not a matchcoded address")
        }

        def parts = field.split('/')
        if (parts.length != 6) {
            throw new IllegalArgumentException("$field is not a matchcoded address")
        }

        def address = addressPool.fetch { Address addr ->
            addr.number = Integer.parseInt(parts[1])
            addr.way = parts[2]
            addr.name = parts[3]
            addr.postBox = Integer.parseInt(parts[4])
            addr.roadNumber = Integer.parseInt(parts[5])
        }

        return address

    }

    @Override
    def reinit() {
        addressPool.clearPool()
    }
}
