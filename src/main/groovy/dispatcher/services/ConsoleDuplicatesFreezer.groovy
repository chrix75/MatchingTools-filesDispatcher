package dispatcher.services

import dispatcher.repositories.DuplicatesRepo

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by batman on 11/09/2016.
 */
class ConsoleDuplicatesFreezer extends Thread implements DuplicatesFreezer {

    private BlockingQueue<DuplicatesRepo> queue
    private AtomicBoolean working

    ConsoleDuplicatesFreezer() {
        this.working = new AtomicBoolean(true)
        this.queue = new ArrayBlockingQueue<>(10)
    }


    @Override
    void freeze(DuplicatesRepo repo) {
        queue.offer(repo)
    }

    @Override
    void markAsFinished() {
        working.set(false)
    }

    @Override
    void run() {
        while (working.get()) {
            DuplicatesRepo repo
            while ((repo = queue.poll(1, TimeUnit.SECONDS))) {
                repo.duplicates.each { recId, groupId ->
                    println "$recId => $groupId"
                }
            }
        }
    }

    static DuplicatesFreezer prepare() {
        def freezer = new ConsoleDuplicatesFreezer()
        freezer.start()

        return freezer
    }
}
