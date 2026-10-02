package trc.rfid.scan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TagTest {
    @Test fun rssiAcceptsDotCommaAndSpaces() {
        assertEquals(-62.5, rssiFromText("-62.5")!!, 0.0)
        assertEquals(-62.5, rssiFromText(" -62,5 ")!!, 0.0)
        assertNull(rssiFromText(null))
        assertNull(rssiFromText(""))
        assertNull(rssiFromText("n/a"))
    }

    @Test fun triggerKeys() {
        assertTrue(RfidScanner.isTrigger(293))
        assertFalse(RfidScanner.isTrigger(4)) // back key
    }
}
