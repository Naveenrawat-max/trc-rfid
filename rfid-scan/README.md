# rfid-scan

Scan UHF tags on a Chainway handheld (C72 and other UART models) and read their memory banks.
Package `trc.rfid.scan`. Part of [trc-rfid](../README.md); [rfid-find](../rfid-find/README.md) builds on it.

```kotlin
implementation("com.github.Naveenrawat-max.trc-rfid:rfid-scan:1.0.0") // brings the Chainway SDK
```

## Names

| You want to… | Call |
|---|---|
| make the scanner | `val scanner = RfidScanner(context)` |
| switch the reader on (once, off the main thread, 1-2 s) | `scanner.connect()` → `true` / `false` (no UHF module) |
| get every tag while scanning (main thread) | `scanner.onTag = { tag -> ... }` |
| start / stop scanning | `scanner.startScan()` / `scanner.stopScan()` |
| read the one nearest tag (off main thread) | `scanner.readOnce()` → `Tag?` |
| read a memory bank (off main thread) | `scanner.readMemory(epc, Bank.USER, start = 0, words = 4)` → hex `String?` |
| switch the reader off (onDestroy) | `scanner.disconnect()` |
| is it on / is it scanning? | `scanner.isReady` / `scanner.isScanning` |
| change range | `scanner.power = 20` (dBm, 1-30; used on next connect/startScan) |
| is this key the trigger? | `RfidScanner.isTrigger(keyCode)` |

`Tag(epc, tid, rssi)`: `epc` and `tid` are hex text (`""` = not read), `rssi` is dBm (`-62.5`) or `null`.
`Bank`: `RESERVED`, `EPC`, `TID`, `USER`.

## Example

```kotlin
class MainActivity : AppCompatActivity() {
    private val scanner by lazy { RfidScanner(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        thread { scanner.connect() }
        scanner.onTag = { tag -> Log.i("scan", "${tag.epc} ${tag.tid} ${tag.rssi}") }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (!RfidScanner.isTrigger(keyCode)) return super.onKeyDown(keyCode, event)
        if (event.repeatCount == 0) if (scanner.isScanning) scanner.stopScan() else scanner.startScan()
        return true
    }

    // All details of one tag: point at it, then (off the main thread)
    fun details() = thread {
        val tag = scanner.readOnce() ?: return@thread
        val user = scanner.readMemory(tag.epc, Bank.USER, 0, 4)        // null if the chip has no/less user memory
        val reserved = scanner.readMemory(tag.epc, Bank.RESERVED, 0, 4) // kill + access password; usually locked → null
    }

    override fun onPause() { super.onPause(); scanner.stopScan() }
    override fun onDestroy() { scanner.disconnect(); super.onDestroy() }
}
```

## Good to know

- Scanning reads EPC **and** TID of every tag (EPC+TID mode). The same tag arrives many times per second: de-duplicate yourself.
- `connect()` clears select filters other apps left on the module, and `startScan()` re-applies EPC+TID mode and power,
  because Chainway's *Keyboard Emulator* changes them. Turn its RFID output off: two apps cannot own the module.
- If the module refuses `startScan()`, it is reconnected in the background; the next press works.
- `readOnce()` and `readMemory()` return `null` while scanning: `stopScan()` first. Both are new in this package and
  not yet verified on a physical C72.
- `readMemory` counts in 16-bit **words** (4 hex characters each). TID is usually 6 words; USER memory size depends
  on the chip (often 0-32 words). Asking past the end gives `null`.
- Tags are never written.

## Chainway SDK

`libs/DeviceAPI_ver20251103_release.aar` is compile-only in this module (a library AAR cannot contain another local AAR)
and is published beside it as `chainway-sdk`; the rfid-scan POM depends on it, so apps receive it automatically.
Updating the SDK: replace the file, update the name in `build.gradle.kts`, release a new tag.

Tests: `./gradlew :rfid-scan:test`.
