package dispatcher

import dispatcher.repositories.DuplicatesRepo
import dispatcher.repositories.MemoryDuplicatesRepo
import domain.Address
import domain.CompanyName
import domain.Match
import domain.Record
import matchingtools.matching.CompanyMatcher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import dispatcher.services.UnmatchcoderAddress
import dispatcher.services.UnmatchcoderCompanyName

import static groovyx.gpars.GParsPool.withPool

/**
 * Created by batman on 28/08/2016.
 */
@SpringBootApplication
class FilesDispatcher implements CommandLineRunner {
    private static Logger logger = LoggerFactory.getLogger(FilesDispatcher.class)
    private int threadsNumber

    UnmatchcoderAddress unmatchcoderAddress = new UnmatchcoderAddress()
    UnmatchcoderCompanyName unmatchcoderCompanyName = new UnmatchcoderCompanyName()

    int siretIdx
    int cityIdx
    int recIdIdx

    private CompanyMatcher companyMatcher = new CompanyMatcher()

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

        def randomAccess = new RandomAccessFile(f, 'r')

        String line
        int count = 0

        DuplicatesRepo duplicatesRepo = new MemoryDuplicatesRepo()

        while ((line = randomAccess.readLine())) {
            def utfLine = convertIntoUtf8(line)
            ++count
            long nextRecordPos = randomAccess.getFilePointer()

            compareNextRecords(utfLine, randomAccess, duplicatesRepo)

            randomAccess.seek(nextRecordPos)
        }

        logger.info("File $f has $count lines")
        logger.info("Duplicates: ${duplicatesRepo.getDuplicates()}")

    }

    def compareNextRecords(String record, RandomAccessFile file, DuplicatesRepo duplicatesRepo) {
        String line
        int count = 0

        def refFields = record.split(';')

        if (refFields.length <= recIdIdx) {
            logger.error("Ref fields $refFields has no enough field to get the record ID (recIdIdx=$recIdIdx)")
            return
        }

        if (!duplicatesRepo.getGroupId(Long.parseLong(refFields[recIdIdx]))) {

            def refRecord = buildRecord(refFields)

            while ((line = file.readLine())) {
                def utfLine = convertIntoUtf8(line)

                def comparedFields = utfLine.split(';')

                if (comparedFields.length <= recIdIdx) {
                    logger.error("Compared fields $refFields has no enough field to get the record ID (recIdIdx=$recIdIdx)")
                    continue
                }


                long comparedRecId = Long.parseLong(comparedFields[recIdIdx])
                if (duplicatesRepo.getGroupId(comparedRecId)) {
                    continue
                }

                def comparedRecord = buildRecord(comparedFields)

                def match = companyMatcher.match(refRecord, comparedRecord)

                println "Compare [$record] with [$utfLine] => $match"

                if (match == Match.MATCH) {
                    def refRecId = Long.parseLong(refFields[recIdIdx])
                    duplicatesRepo.addDuplicates(refRecId, comparedRecId)
                }

                ++count
            }
        }
    }

    Record buildRecord(String[] fields) {
        def refAddresses = extractAddresses(fields)
        def refCompanyNames = extractCompanyNames(fields)
        def siret = siretIdx >= 0 && siretIdx < fields.size() ? fields[siretIdx] : ""
        def city = cityIdx < fields.size() ? fields[cityIdx] : ""

        new Record(refCompanyNames, refAddresses, siret, city)
    }

    List<CompanyName> extractCompanyNames(String[] fields) {
        fields.findAll { it.startsWith('RS/') }.collect { unmatchcoderCompanyName.unmatchcode(it)}
    }

    List<Address> extractAddresses(String[] fields) {
        fields.findAll { it.startsWith('ADR/') }.collect { unmatchcoderAddress.unmatchcode(it)}
    }

    String convertIntoUtf8(String inp) {
        byte[] bytes = new byte[inp.length()];
        for (int i = 0; i < inp.length(); i++)
            bytes[i] = (byte)inp.charAt(i);

        new String(bytes, "UTF-8");
    }

    public static void main(String[] args) {
        SpringApplication.run(FilesDispatcher.class, args);
    }

    @Override
    void run(String... args) throws Exception {
        def cl = new CliBuilder(usage: 'filedispatcher --siret siretField --city cityField --recid recIdField matchcodeFileList')
        cl.siret(argName: 'siret', args: 1, longOpt: 'siret', 'SIRET field number', required: true)
        cl.city(argName: 'city', args: 1, longOpt:  'city', 'City field number')
        cl.recid(argName: 'recid', args:1, longOpt: 'recid', 'Record ID field number', required: true)

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
