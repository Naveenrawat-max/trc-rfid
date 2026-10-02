/** Chainway UHF reader (C72 and other UART models): connect, scan EPC+TID continuously, read one tag, read a memory bank. */
package trc.rfid.scan

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.rscja.deviceapi.RFIDWithUHFUART
import com.rscja.deviceapi.entity.UHFTAGInfo
import com.rscja.deviceapi.interfaces.IUHF
import com.rscja.deviceapi.interfaces.IUHFInventoryCallback
import kotlin.concurrent.thread

/**
 * Typical use:
 * ```
 * val scanner = RfidScanner(context)
 * thread { scanner.connect() }               // once, off the main thread
 * scanner.onTag = { tag -> show(tag.epc) }   // called on the main thread
 * scanner.startScan() ... scanner.stopScan()
 * scanner.disconnect()                       // in onDestroy
 * ```
 */
class RfidScanner(private val context: Context) {
    private val main = Handler(Looper.getMainLooper())
    private var uhf: RFIDWithUHFUART? = null

    /** Output power in dBm, used on connect and on every startScan. C72 range 1-30. */
    // ponytail: calibration knob; 30 dBm gives ~5 m on a C72, lower it if scans pick up too many neighbours.
    var power = 30

    /** True after a successful [connect]. False on devices without a UHF module. */
    @Volatile var isReady = false
        private set
    @Volatile var isScanning = false
        private set
    @Volatile private var closed = false

    /** Every tag read while scanning, on the main thread. The same tag arrives many times per second. */
    var onTag: ((Tag) -> Unit)? = null

    private val callback = IUHFInventoryCallback { info: UHFTAGInfo ->
        val tag = info.toTag()
        main.post { if (isScanning) onTag?.invoke(tag) }
    }

    /** Blocking (1-2 s): call off the main thread. Returns false on devices without the UHF module. */
    fun connect(): Boolean = try {
        val r = RFIDWithUHFUART.getInstance()
        if (r.init(context.applicationContext)) {
            // Another app may have left a select filter on the module; clear it so every tag is read.
            r.setFilter(IUHF.Bank_EPC, 0, 0, "")
            r.setFilter(IUHF.Bank_TID, 0, 0, "")
            r.setFilter(IUHF.Bank_USER, 0, 0, "")
            // Read both banks, so a tag can be recognised by EPC or by TID.
            if (!r.setEPCAndTIDMode()) Log.w(TAG, "Module did not confirm EPC+TID mode")
            if (!r.setPower(power)) Log.w(TAG, "Module did not accept $power dBm")
            uhf = r
            isReady = true
            Log.i(TAG, "UHF ready: ${r.getVersion()}")
            true
        } else {
            Log.w(TAG, "UHF init returned false")
            false
        }
    } catch (t: Throwable) {
        Log.w(TAG, "UHF reader unavailable: ${t.javaClass.simpleName}")
        false
    }

    /** False when the module refused; it is then reconnected in the background so the next press works. */
    fun startScan(): Boolean {
        val r = uhf ?: return false
        if (isScanning) return true
        if (r.isInventorying()) r.stopInventory() // left running by a crash or another screen
        r.setEPCAndTIDMode() // another app (e.g. Keyboard Emulator) may have switched modes while we were paused
        r.setPower(power) // ...or lowered the output power
        r.setInventoryCallback(callback)
        isScanning = r.startInventoryTag()
        if (!isScanning) {
            Log.w(TAG, "startInventoryTag refused; reconnecting the module")
            uhf = null
            isReady = false
            thread(name = "uhf-reconnect") {
                runCatching { r.free() }
                if (connect() && closed) disconnect() // the app closed meanwhile
            }
        }
        return isScanning
    }

    fun stopScan() {
        if (!isScanning) return
        isScanning = false
        uhf?.stopInventory()
    }

    /** Reads the single nearest tag once (EPC + TID). Blocking: call off the main thread, not while scanning. Null = no tag. */
    fun readOnce(): Tag? {
        val r = uhf ?: return null
        if (isScanning) return null
        return r.inventorySingleTag()?.toTag()?.takeIf { it.epc.isNotEmpty() }
    }

    /**
     * Reads [words] 16-bit words from [bank], starting at word [start], as hex text (4 characters per word).
     * [epc] picks which tag answers; empty = whichever tag answers first. [password] is the tag's access password
     * (8 hex characters; "00000000" for unlocked tags).
     * Blocking: call off the main thread, not while scanning. Null when the tag did not answer or the range does not exist.
     *
     * Common reads: `readMemory(epc, Bank.TID, 0, 6)`, `readMemory(epc, Bank.USER, 0, 4)`,
     * `readMemory(epc, Bank.RESERVED, 0, 4)` (kill + access password; usually locked).
     */
    fun readMemory(epc: String, bank: Bank, start: Int, words: Int, password: String = "00000000"): String? {
        val r = uhf ?: return null
        if (isScanning) return null
        val target = epc.trim()
        val data = if (target.isEmpty()) {
            r.readData(password, bank.sdk, start, words)
        } else {
            // The EPC starts at bit 32 of the EPC bank (after CRC and PC); filter offset and length are in bits.
            r.readData(password, IUHF.Bank_EPC, 32, target.length * 4, target, bank.sdk, start, words)
        }
        return data?.takeIf { it.isNotEmpty() }
    }

    /** Stops scanning and releases the module so other apps can use it. Call in onDestroy. */
    fun disconnect() {
        closed = true
        stopScan()
        uhf?.free()
        uhf = null
        isReady = false
    }

    companion object {
        private const val TAG = "RfidScanner"

        /** Hardware trigger and side keys of Chainway handhelds (from the SDK demo). */
        val TRIGGER_KEYS = setOf(139, 280, 291, 293, 294, 311, 312, 313, 315, 591, 593, 594, 596)

        /** True for the scan trigger: use in onKeyDown to start/stop scanning. */
        fun isTrigger(keyCode: Int): Boolean = keyCode in TRIGGER_KEYS
    }
}

private val Bank.sdk: Int
    get() = when (this) {
        Bank.RESERVED -> IUHF.Bank_RESERVED
        Bank.EPC -> IUHF.Bank_EPC
        Bank.TID -> IUHF.Bank_TID
        Bank.USER -> IUHF.Bank_USER
    }

private fun UHFTAGInfo.toTag() = Tag(getEPC().orEmpty(), getTid().orEmpty(), rssiFromText(getRssi()))
