package dispatcher;

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * Created by batman on 28/08/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes=DispatcherConfiguration.class)
public class FilesDispatcherTest {

    @Autowired
    FilesDispatcher dispatcher

    @Test
    public void processFiles() throws Exception {
        def files = ["A", "B", "C", "D", "E", "F", "G", "H"].collect { new File(it) }
        def dispatcher = new FilesDispatcher(2)
        dispatcher.processFiles(files)
    }

    @Test
    public void processOneFile() {
        def f = new File('/Users/batman/IdeaProjects/matchingtools/MatchingTools-matchcoder/src/test/resources/DKV.part_73110')
        dispatcher.siretIdx = 0
        dispatcher.cityIdx = 4
        dispatcher.recIdIdx = 6
        dispatcher.processOneFile(f)
    }

}