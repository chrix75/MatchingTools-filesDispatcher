package dispatcher.services

import domain.Address

/**
 * Exemple: ADR/1//Rue De Dhuisy/0/0
 * Created by batman on 28/08/2016.
 */
class UnmatchcoderAddress implements Unmatchcoder<Address> {
    @Override
    Address unmatchcode(String field) {
        if (!field.startsWith("ADR/")) {
            throw new IllegalArgumentException("$field is not a matchcoded address")
        }

        def parts = field.split('/')
        if (parts.length != 6) {
            throw new IllegalArgumentException("$field is not a matchcoded address")
        }

        parts[1..-1] as Address

    }
}
