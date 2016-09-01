package dispatcher.repositories

/**
 * Created by batman on 29/08/2016.
 */
interface DuplicatesRepo {

    UUID addDuplicates(long recid1, long recid2)
    UUID getGroupId(long recid)

    Map<Long, UUID> getDuplicates()
}
