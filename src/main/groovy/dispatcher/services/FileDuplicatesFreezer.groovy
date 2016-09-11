package dispatcher.services

import dispatcher.repositories.DuplicatesRepo

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by batman on 11/09/2016.
 */
class FileDuplicatesFreezer extends Thread implements dispatcher.services.DuplicatesFreezer {
    private BlockingQueue<DuplicatesRepo> queue
    private AtomicBoolean working
    private BufferedOutputStream outputFile

    FileDuplicatesFreezer(File file) {
        this.working = new AtomicBoolean(true)
        this.queue = new ArrayBlockingQueue<>(10)
        this.outputFile = file.newOutputStream()
    }


    @Override
    void freeze(DuplicatesRepo repo) {
        queue.offer(repo)
    }

    @Override
    void markAsFinished() {
        working.set(false)
        join()
    }

    @Override
    void run() {
        while (working.get()) {
            DuplicatesRepo repo
            while ((repo = queue.poll(1, TimeUnit.SECONDS))) {
                repo.duplicates.each { recId, groupId ->
                    outputFile.write("$recId;$groupId\n".bytes)
                }
            }
        }

        outputFile.close()
    }

    static DuplicatesFreezer prepare(File outputFile) {
        def freezer = new FileDuplicatesFreezer(outputFile)
        freezer.start()

        return freezer
    }
}
