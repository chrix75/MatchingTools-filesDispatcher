package dispatcher.readers

import dispatcher.repositories.DuplicatesRepo
import dispatcher.services.UnmatchcoderAddress
import dispatcher.services.UnmatchcoderCompanyName
import domain.Address
import domain.CompanyName
import domain.Record
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by Christian Sperandio on 04/09/2016.
 */
class FileMatchMaker implements MatchMaker {
    private static Logger logger = LoggerFactory.getLogger(FileMatchMaker.class)

    File file
    int recIdIdx
    int siretIdx
    int cityIdx

    DuplicatesRepo duplicatesRepo

    UnmatchcoderAddress unmatchcoderAddress = new UnmatchcoderAddress()
    UnmatchcoderCompanyName unmatchcoderCompanyName = new UnmatchcoderCompanyName()


    FileMatchMaker(File f, int recIdIdx, int siretIdx, int cityIdx) {
        this.file = f
        this.recIdIdx = recIdIdx
        this.siretIdx = siretIdx
        this.cityIdx = cityIdx
    }

    @Override
    def processCouple(DuplicatesRepo repo, Closure coupleAction) {
        this.duplicatesRepo = repo
        def randomAccess = new RandomAccessFile(file, 'r')

        String line
        try {
            while ((line = randomAccess.readLine())) {
                def utfLine = convertIntoUtf8(line)
                long nextRecordPos = randomAccess.getFilePointer()

                compareNextRecords(utfLine, randomAccess, coupleAction)

                randomAccess.seek(nextRecordPos)
            }
        } finally {
            randomAccess.close()
        }
    }

    def compareNextRecords(String record, RandomAccessFile file, Closure action) {
        String line

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

                if (refRecord && comparedRecord) {
                    action(refRecord, comparedRecord)
                }
            }
        }
    }

    Record buildRecord(String[] fields) {
        def refAddresses = extractAddresses(fields)
        def refCompanyNames = extractCompanyNames(fields)
        def siret = siretIdx >= 0 && siretIdx < fields.size() ? fields[siretIdx] : ""
        def city = cityIdx < fields.size() ? fields[cityIdx] : ""
        def recId = Long.parseLong(fields[recIdIdx])

        new Record(recId, refCompanyNames, refAddresses, siret, city)
    }

    String convertIntoUtf8(String inp) {
        byte[] bytes = new byte[inp.length()];
        for (int i = 0; i < inp.length(); i++)
            bytes[i] = (byte) inp.charAt(i);

        new String(bytes, "UTF-8");
    }


    List<CompanyName> extractCompanyNames(String[] fields) {
        fields.findAll { it.startsWith('RS/') }.collect { unmatchcoderCompanyName.unmatchcode(it)}
    }

    List<Address> extractAddresses(String[] fields) {
        fields.findAll { it.startsWith('ADR/') }.collect { unmatchcoderAddress.unmatchcode(it)}
    }


}
