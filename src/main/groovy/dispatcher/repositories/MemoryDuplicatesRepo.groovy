package dispatcher.repositories

/**
 * Created by batman on 29/08/2016.
 */
class MemoryDuplicatesRepo implements DuplicatesRepo {
    private Map<Long, Long> groups = new HashMap<>()
    long id = 0

    @Override
    long addDuplicates(long recid1, long recid2) {
        def groupId = ++id
        groups.put(recid1, groupId)
        groups.put(recid2, groupId)

        return groupId
    }

    @Override
    long getGroupId(long recid) {
        def found = groups.get(recid)
        return !found ? 0 : found
    }

    @Override
    Map<Long, Long> getDuplicates() {
        return groups
    }
}
