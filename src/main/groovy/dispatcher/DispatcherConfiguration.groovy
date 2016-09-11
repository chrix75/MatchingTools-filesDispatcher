package dispatcher

import dispatcher.repositories.DuplicatesRepo
import dispatcher.repositories.MemoryDuplicatesRepo
import dispatcher.services.ConsoleDuplicatesFreezer
import dispatcher.services.DuplicatesFreezer
import dispatcher.services.FileDuplicatesFreezer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Created by batman on 29/08/2016.
 */
@Configuration
class DispatcherConfiguration {
    @Bean
    DuplicatesRepo duplicatesRepo() {
        new MemoryDuplicatesRepo()
    }

    @Bean
    FilesDispatcher filesDispatcher() {
        new FilesDispatcher()
    }

    @Bean
    DuplicatesFreezer duplicatesFreezer() {
        FileDuplicatesFreezer.prepare(new File('/Volumes/Comics01/test/duplicates.out'))
    }
}
