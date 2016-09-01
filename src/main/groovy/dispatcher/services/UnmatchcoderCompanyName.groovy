package dispatcher.services

import domain.CompanyName

/**
 * Created by batman on 28/08/2016.
 */
class UnmatchcoderCompanyName implements Unmatchcoder<CompanyName> {
    @Override
    CompanyName unmatchcode(String field) {
        if (!field.startsWith("RS/")) {
            throw new IllegalArgumentException("$field is not the matchcode of a company name")
        }

        def parts = field.split("/")
        if (parts.length != 3) {
            throw new IllegalArgumentException("$field is not the matchcode of a company name")
        }

        boolean isService = parts[1] == 'true'
        def words = parts[2].tokenize(', ')

        new CompanyName(isService, words)
    }
}
