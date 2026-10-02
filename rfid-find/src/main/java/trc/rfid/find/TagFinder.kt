/** Hot/cold finding of one tag: matches scanner reads to the target and turns RSSI into a 0-100 signal and a word. */
package trc.rfid.find

import android.os.SystemClock
import trc.rfid.scan.Tag
import kotlin.math.roundToInt

/**
 * Follows one physical tag while the scanner runs. Make a new TagFinder for every find run.
 *
 * [epcs] match only a read's EPC, [tids] only its TID (case and spaces ignored). Unsure which bank a value is?
 * Put it in both. Other tags never move the signal.
 *
 * Far away the reader often gets a tag's EPC but not its TID (the TID needs a second, weaker exchange). So once a
 * read matched by TID, the EPC reported with it counts as the same tag for the rest of this run.
 * ponytail: tags sharing a factory-default EPC would then also count; fine while tags have distinct EPCs.
 *
 * RSSI is not a distance: [signal] and [label] are relative proximity, never metres.
 *
 * ```
 * val finder = TagFinder(epcs = listOf("E200..."))
 * scanner.onTag = { tag -> finder.onTag(tag) }
 * // every ~250 ms:
 * val signal = finder.signal()                    // 0..100, or null = not heard
 * text = signal?.let { TagFinder.label(it) } ?: if (finder.seen) "Signal lost" else "Searching"
 * ```
 */
class TagFinder(
    epcs: Collection<String> = emptyList(),
    tids: Collection<String> = emptyList(),
    /** No matching read for this long = signal lost. Far reads are sparse, so keep it at a few seconds. */
    private val lostAfterMs: Long = 3000,
    // ponytail: calibration knobs. -85 dBm is roughly the weakest read a C72 reports at ~5 m; tune on the device.
    /** This RSSI (dBm) or weaker = signal 0. */
    private val weakestRssi: Double = -85.0,
    /** This RSSI (dBm) or stronger = signal 100. */
    private val strongestRssi: Double = -40.0,
) {
    private val epcs = epcs.map(::norm).filter { it.isNotEmpty() }.toSet()
    private val tids = tids.map(::norm).filter { it.isNotEmpty() }.toSet()
    private val pairedEpcs = HashSet<String>()
    private var smoothed: Double? = null
    private var lastSeen = -1L

    /** False when there is nothing to look for (no EPC and no TID given). */
    val hasTarget: Boolean get() = epcs.isNotEmpty() || tids.isNotEmpty()
    /** A matching read arrived during this run: no signal now means "lost", not "still searching". */
    val seen: Boolean get() = lastSeen >= 0
    /** RSSI (dBm) of the last matching read, unsmoothed. */
    var lastRssi: Double? = null
        private set

    /** Feed every scanner read here. True when the read belongs to the target. */
    fun onTag(tag: Tag, now: Long = SystemClock.elapsedRealtime()): Boolean {
        val e = norm(tag.epc)
        val byTid = norm(tag.tid) in tids
        if (!byTid && e !in epcs && e !in pairedEpcs) return false
        if (byTid && e.isNotEmpty()) pairedEpcs += e
        lastSeen = now
        tag.rssi?.let {
            lastRssi = it
            smoothed = smoothed?.let { s -> s * 0.6 + it * 0.4 } ?: it
        }
        return true
    }

    /** 0..100 (smoothed), or null when the tag has not been heard within [lostAfterMs]. */
    fun signal(now: Long = SystemClock.elapsedRealtime()): Int? {
        if (lastSeen < 0 || now - lastSeen > lostAfterMs) {
            smoothed = null
            return null
        }
        return percent(smoothed ?: return 0)
    }

    /** [weakestRssi] or weaker maps to 0, [strongestRssi] or stronger to 100. */
    fun percent(rssi: Double): Int =
        ((rssi - weakestRssi) / (strongestRssi - weakestRssi) * 100.0).roundToInt().coerceIn(0, 100)

    companion object {
        /** A signal 0..100 as a word: Very far, Far, Mid, Close, Very close. */
        fun label(signal: Int): String = when {
            signal < 20 -> "Very far"
            signal < 40 -> "Far"
            signal < 60 -> "Mid"
            signal < 80 -> "Close"
            else -> "Very close"
        }

        private fun norm(value: String): String = value.trim().uppercase()
    }
}
