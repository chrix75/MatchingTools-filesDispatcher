package services

import dispatcher.services.UnmatchcoderCompanyName;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by batman on 28/08/2016.
 */
public class UnmatchcoderCompanyNameTest {
    @Test
    public void unmatchcode_1() throws Exception {
        def unmatchcoder = new UnmatchcoderCompanyName()

        def  unmatchcodedName = unmatchcoder.unmatchcode("RS/false/BEAUVAIS,INTERNATIONAL,S.A.S  (BTE)")
        assertFalse(unmatchcodedName.isService)
        assertEquals(["BEAUVAIS", "INTERNATIONAL", "S.A.S  (BTE)"], unmatchcodedName.wordsName)
    }

    @Test
    public void unmatchcode_2() throws Exception {
        def unmatchcoder = new UnmatchcoderCompanyName()

        def  unmatchcodedName = unmatchcoder.unmatchcode("RS/true/SERVICE,MEDITERRANEE,TRANSPORT")
        assertTrue(unmatchcodedName.isService)
        assertEquals(["SERVICE", "MEDITERRANEE", "TRANSPORT"], unmatchcodedName.wordsName)
    }

}