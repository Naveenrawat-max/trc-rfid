package trc.rfid.find

import trc.rfid.scan.Tag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TagFinderTest {
    private fun either(value: String) = TagFinder(epcs = listOf(value), tids = listOf(value))
    private fun tag(epc: String = "", tid: String = "", rssi: Double? = -40.0) = Tag(epc, tid, rssi)

    @Test fun percentScalesAndClamps() {
        val f = TagFinder()
        assertEquals(0, f.percent(-95.0))
        assertEquals(0, f.percent(-85.0))
        assertEquals(50, f.percent(-62.5))
        assertEquals(100, f.percent(-40.0))
        assertEquals(100, f.percent(-20.0))
        assertEquals(50, TagFinder(weakestRssi = -70.0, strongestRssi = -50.0).percent(-60.0))
    }

    @Test fun labelsAreRelativeProximity() {
        assertEquals("Very far", TagFinder.label(0))
        assertEquals("Very far", TagFinder.label(19))
        assertEquals("Far", TagFinder.label(20))
        assertEquals("Mid", TagFinder.label(40))
        assertEquals("Close", TagFinder.label(60))
        assertEquals("Very close", TagFinder.label(80))
        assertEquals("Very close", TagFinder.label(100))
    }

    @Test fun matchesEpcOrTidCaseInsensitively() {
        val f = either("A1000000000000000000000A")
        assertFalse(f.onTag(tag("3000AAAA", "B2000000000000000000000B"), 1000))
        assertTrue(f.onTag(tag("3000AAAA", "a1000000000000000000000a", -62.5), 1000))
        assertEquals(50, f.signal(1000))
        assertEquals(-62.5, f.lastRssi!!, 0.0)
    }

    @Test fun smoothsSignal() {
        val f = either("ABC")
        f.onTag(tag("ABC", rssi = -85.0), 1)
        f.onTag(tag("ABC", rssi = -40.0), 2)
        assertEquals(40, f.signal(2)) // 0.6 * -85 + 0.4 * -40 = -67 dBm
    }

    @Test fun forgetsTagAfterTimeout() {
        val f = either("ABC")
        assertNull(f.signal(0))
        f.onTag(tag("ABC"), 1000)
        assertNotNull(f.signal(3900)) // sparse far reads still count
        assertNull(f.signal(4100))
    }

    @Test fun withoutTargetsNeverMatches() {
        val f = TagFinder(epcs = listOf(" "))
        assertFalse(f.hasTarget)
        assertFalse(f.onTag(tag("ABC", "DEF"), 1))
    }

    @Test fun epcOnlyMatchesEpcAndTidOnlyTid() {
        val epc = TagFinder(epcs = listOf("ABC"))
        assertFalse(epc.onTag(tag(tid = "ABC"), 1))
        assertTrue(epc.onTag(tag("abc", "XYZ"), 1))
        val tid = TagFinder(tids = listOf("ABC"))
        assertFalse(tid.onTag(tag(epc = "ABC"), 1))
        assertTrue(tid.onTag(tag(tid = "ABC"), 1))
    }

    /** At range the TID read fails and only the EPC comes back: the EPC seen with the matching TID keeps the signal. */
    @Test fun epcSeenWithMatchingTidKeepsMatchingWithoutTid() {
        val f = TagFinder(tids = listOf("T1"))
        assertFalse(f.onTag(tag("E1", rssi = -80.0), 1)) // not yet known to belong to the target
        assertTrue(f.onTag(tag("E1", "T1", -50.0), 2))
        assertTrue(f.onTag(tag("e1", rssi = -80.0), 3))
        assertFalse(f.onTag(tag("E2"), 4)) // another tag still never counts
        assertFalse(TagFinder(tids = listOf("T1")).onTag(tag("E1"), 5)) // a new run starts clean
    }

    @Test fun searchingThenSignalLost() {
        val f = either("ABC")
        assertFalse(f.seen) // "Searching"
        f.onTag(tag("ABC"), 1000)
        assertTrue(f.seen)
        assertNull(f.signal(5000)) // seen, then silent past the timeout: "Signal lost", no stale strength
    }
}
