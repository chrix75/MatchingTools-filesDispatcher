package dispatcher.repositories

/**
 * Created by batman on 29/08/2016.
 */
interface DuplicatesRepo {

    long addDuplicates(long recid1, long recid2)
    long getGroupId(long recid)

    Map<Long, Long> getDuplicates()
}
