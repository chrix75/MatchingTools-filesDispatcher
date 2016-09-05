package dispatcher

import dispatcher.readers.FileMatchMaker
import dispatcher.repositories.MemoryDuplicatesRepo
import domain.Match
import matchingtools.matching.CompanyMatcher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

import static groovyx.gpars.GParsPool.withPool
/**
 * Created by batman on 28/08/2016.
 */
@SpringBootApplication
class FilesDispatcher implements CommandLineRunner {
    private static Logger logger = LoggerFactory.getLogger(FilesDispatcher.class)
    private int threadsNumber

    private int siretIdx
    private int cityIdx
    private int recIdIdx

    FilesDispatcher(int threadsNumber = 0) {
        this.threadsNumber = threadsNumber
    }

    def processFiles(List<File> files, int siretIdx, int cityIdx, int recIdIdx) {
        this.siretIdx = siretIdx
        this.cityIdx = cityIdx
        this.recIdIdx = recIdIdx

        if (threadsNumber) {
            withPool(threadsNumber) {
                files.eachParallel { processOneFile(it) }
            }
        } else {
            withPool {
                files.eachParallel { processOneFile(it) }
            }
        }
    }

    def processOneFile(File f) {
        logger.info("Start processing file $f")

        def matchMaker = new FileMatchMaker(f, recIdIdx, siretIdx, cityIdx)

        def duplicatesRepo = new MemoryDuplicatesRepo()

        def CompanyMatcher companyMatcher = new CompanyMatcher()

        matchMaker.processCouple(duplicatesRepo) {
            ref, other ->

                def match = companyMatcher.match(ref, other)

                println "Compare [$ref] with [$other] => $match"

                if (match == Match.MATCH) {
                    duplicatesRepo.addDuplicates(ref.recorId, other.recorId)
                }

        }

        logger.info("Duplicates: ${duplicatesRepo.getDuplicates()}")

    }

    public static void main(String[] args) {
        SpringApplication.run(FilesDispatcher.class, args);
    }

    @Override
    void run(String... args) throws Exception {
        def cl = new CliBuilder(usage: 'filedispatcher --siret siretField --city cityField --recid recIdField matchcodeFileList')
        cl.siret(argName: 'siret', args: 1, longOpt: 'siret', 'SIRET field number', required: true)
        cl.city(argName: 'city', args: 1, longOpt: 'city', 'City field number')
        cl.recid(argName: 'recid', args: 1, longOpt: 'recid', 'Record ID field number', required: true)

        def opt = cl.parse(args)

        if (!opt) {
            println cl.usage
            System.exit(1)
        }

        def lstFile = opt.arguments()[0]
        def listFile = new File(lstFile)
        def files = listFile.readLines().collect { new File(it) }
        def siretIdx = Integer.parseInt(opt.siret) - 1
        def cityIdx = Integer.parseInt(opt.city) - 1
        def recIdIdx = Integer.parseInt(opt.recid) - 1

        processFiles(files, siretIdx, cityIdx, recIdIdx)
    }
}
