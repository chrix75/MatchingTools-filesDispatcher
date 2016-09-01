package services

import dispatcher.services.UnmatchcoderAddress;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by batman on 28/08/2016.
 */
public class UnmatchcoderAddressTest {
    @Test
    public void unmatchcode() throws Exception {
        def unmatchcoder = new UnmatchcoderAddress()
        def field = "ADR/1/RUE/DHUISY/2/3"
        def address = unmatchcoder.unmatchcode(field)

        assertEquals(1, address.number)
        assertEquals("RUE", address.way)
        assertEquals("DHUISY", address.name)
        assertEquals(2, address.postBox)
        assertEquals(3, address.roadNumber)
    }

}