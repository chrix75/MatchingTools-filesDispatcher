package dispatcher.readers

import dispatcher.repositories.DuplicatesRepo

/**
 * Created by Christian Sperandio on 04/09/2016.
 */
interface MatchMaker {
    def processCouple(DuplicatesRepo repo, Closure coupleAction)

}