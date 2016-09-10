package dispatcher.readers

import dispatcher.repositories.DuplicatesRepo
import dispatcher.services.UnmatchcoderAddress
import dispatcher.services.UnmatchcoderCompanyName
import domain.Address
import domain.CompanyName
import domain.Record
import matchingtools.matching.CompanyMatcher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import services.ObjectsPool

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

    final Record refRecord
    final Record comparedRecord


    final ObjectsPool<CompanyName> companyNamePool


    FileMatchMaker(File f, int recIdIdx, int siretIdx, int cityIdx) {
        this.file = f
        this.recIdIdx = recIdIdx
        this.siretIdx = siretIdx
        this.cityIdx = cityIdx

        this.refRecord = new Record()
        this.comparedRecord = new Record()


        companyNamePool = new ObjectsPool<>( {new CompanyName()} )
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

                def bufferedReader = new BufferedReader(new FileReader(randomAccess.FD))

                compareNextRecords(utfLine, bufferedReader, coupleAction)

                randomAccess.seek(nextRecordPos)
            }
        } finally {
            randomAccess.close()
        }
    }

    def compareNextRecords(String record, BufferedReader file, Closure action) {
        String line

        def refFields = record.split(';', -1)

        def refKeys = extractKeys(refFields)

        if (refFields.length <= recIdIdx) {
            logger.error("Ref fields $refFields has no enough field to get the record ID (recIdIdx=$recIdIdx)")
            return
        }


        if (!duplicatesRepo.getGroupId(Long.parseLong(refFields[recIdIdx]))) {
            def refRecord = buildRefRecord(refFields)

            while ((line = file.readLine())) {
                def utfLine = convertIntoUtf8(line)

                def comparedFields = utfLine.split(';', -1)

                def comparedKeys = extractKeys(comparedFields)

                if (!validateKeys(refKeys, comparedKeys)) {
                    continue
                }



                if (comparedFields.length <= recIdIdx) {
                    logger.error("Compared fields $refFields has no enough field to get the record ID (recIdIdx=$recIdIdx)")
                    continue
                }

                long comparedRecId = Long.parseLong(comparedFields[recIdIdx])
                if (duplicatesRepo.getGroupId(comparedRecId)) {
                    continue
                }

                def comparedRecord = buildComparedRecord(comparedFields)

                action(refRecord, comparedRecord)
            }
        }
    }

    boolean validateKeys(List<String> keys1, List<String> keys2) {
        keys1.any { keys2.contains(it) }
    }

    List extractKeys(String[] fields) {
        extracItem(fields, 'KEY/') { it.replace('KEY/', '') }
    }

    Record buildRefRecord(String[] fields) {
        def refAddresses = extractAddresses(fields)
        def refCompanyNames = extractCompanyNames(fields)
        def siret = siretIdx >= 0 && siretIdx < fields.size() ? fields[siretIdx] : ""
        def city = cityIdx < fields.size() ? fields[cityIdx] : ""
        def recId = Long.parseLong(fields[recIdIdx])

        refRecord.addresses = refAddresses
        refRecord.names = refCompanyNames
        refRecord.siret = siret
        refRecord.city = city
        refRecord.recorId = recId

        refRecord
    }

    Record buildComparedRecord(String[] fields) {
        def refAddresses = extractAddresses(fields)
        def refCompanyNames = extractCompanyNames(fields)
        def siret = siretIdx >= 0 && siretIdx < fields.size() ? fields[siretIdx] : ""
        def city = cityIdx < fields.size() ? fields[cityIdx] : ""
        def recId = Long.parseLong(fields[recIdIdx])

        comparedRecord.addresses = refAddresses
        comparedRecord.names = refCompanyNames
        comparedRecord.siret = siret
        comparedRecord.city = city
        comparedRecord.recorId = recId

        unmatchcoderAddress.reinit()

        return comparedRecord
    }

    String convertIntoUtf8(String inp) {
        byte[] bytes = new byte[inp.length()];
        for (int i = 0; i < inp.length(); i++)
            bytes[i] = (byte) inp.charAt(i);

        new String(bytes, "UTF-8");
    }

    List extracItem(String[] fields, String prefix, Closure fn) {
        def result = []
        boolean found = false
        for (int i = fields.length - 1; i >= 0; --i) {
            def it = fields[i]
            if (it.startsWith(prefix)) {
                found = true
                result << fn(it)
            } else if (found) {
                break
            }
        }

        result
    }

    List<CompanyName> extractCompanyNames(String[] fields) {
        extracItem(fields, 'RS/', unmatchcoderCompanyName.&unmatchcode)
        //fields.findAll { it.startsWith('RS/') }.collect { unmatchcoderCompanyName.unmatchcode(it)}
    }

    List<Address> extractAddresses(String[] fields) {
        extracItem(fields, 'ADR/', unmatchcoderAddress.&unmatchcode)
        //fields.findAll { it.startsWith('ADR/') }.collect { unmatchcoderAddress.unmatchcode(it)}
    }


}
