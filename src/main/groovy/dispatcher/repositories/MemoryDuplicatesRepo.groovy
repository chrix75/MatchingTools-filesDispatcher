package dispatcher.repositories

/**
 * Created by batman on 29/08/2016.
 */
class MemoryDuplicatesRepo implements DuplicatesRepo {
    private Map<Long, UUID> groups = new HashMap<>()

    @Override
    UUID addDuplicates(long recid1, long recid2) {
        def groupId = UUID.randomUUID()
        groups.put(recid1, groupId)
        groups.put(recid2, groupId)
    }

    @Override
    UUID getGroupId(long recid) {
        groups.get(recid)
    }

    @Override
    Map<Long, UUID> getDuplicates() {
        groups
    }
}
