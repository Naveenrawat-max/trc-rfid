/** One tag read by the scanner: EPC, TID and signal strength. */
package trc.rfid.scan

/**
 * [epc] and [tid] are hex text as the reader sent it; an empty string means that bank was not read.
 * [rssi] is the signal in dBm (e.g. -62.5), null when the reader did not report it.
 */
data class Tag(val epc: String, val tid: String, val rssi: Double?)

/** The four memory banks of a UHF (Gen2) tag. */
enum class Bank { RESERVED, EPC, TID, USER }

/** The reader sends RSSI as text, sometimes with a comma ("-62,5"). */
internal fun rssiFromText(text: String?): Double? = text?.trim()?.replace(',', '.')?.toDoubleOrNull()
