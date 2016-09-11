package dispatcher.services

import dispatcher.repositories.DuplicatesRepo

/**
 * Created by batman on 11/09/2016.
 */
interface DuplicatesFreezer {
    void freeze(DuplicatesRepo repo)
    void markAsFinished()
}