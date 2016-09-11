package dispatcher.repositories

/**
 * Created by batman on 29/08/2016.
 */
class MemoryDuplicatesRepo implements DuplicatesRepo {
    private Map<Long, UUID> groups = new HashMap<>()

    @Override
    UUID addDuplicates(long recid1, long recid2) {
        def groupId = fetchUUID(recid1, recid2)

        groups.put(recid1, groupId)
        groups.put(recid2, groupId)

        return groupId
    }

    UUID fetchUUID(long id1, long id2) {
        UUID uuid

        uuid = getGroupId(id1)
        if (uuid) {
            return uuid
        }

        uuid = getGroupId(id2)
        if (uuid) {
            return uuid
        }

        UUID.randomUUID()
    }

    @Override
    UUID getGroupId(long recid) {
        groups.get(recid)
    }

    @Override
    Map<Long, UUID> getDuplicates() {
        return groups
    }
}
