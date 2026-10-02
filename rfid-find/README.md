# rfid-find

Hot/cold finding of one UHF tag: feed it the reads from [rfid-scan](../rfid-scan/README.md), get a 0-100 signal and
a word (*Very far … Very close*). Package `trc.rfid.find`. Part of [trc-rfid](../README.md); tuned on a C72.

```kotlin
implementation("com.github.Naveenrawat-max.trc-rfid:rfid-find:1.0.0") // brings rfid-scan and the Chainway SDK
```

## Names

| You want to… | Call |
|---|---|
| look for a tag | `val finder = TagFinder(epcs = listOf("E280..."))` or `TagFinder(tids = listOf("E200..."))` |
| unsure if the value is EPC or TID | put it in both: `TagFinder(epcs = listOf(v), tids = listOf(v))` |
| give it a read | `finder.onTag(tag)` → `true` when it is the target |
| how close? | `finder.signal()` → `0..100`, or `null` = not heard in the last 3 s |
| as a word | `TagFinder.label(signal)` → `Very far`, `Far`, `Mid`, `Close`, `Very close` |
| heard it at all this run? | `finder.seen` (`signal() == null && seen` = "Signal lost", else "Searching") |
| anything to look for? | `finder.hasTarget` |
| last raw dBm | `finder.lastRssi` |

## Example

```kotlin
val finder = TagFinder(epcs = listOf(targetEpc))          // a new TagFinder for every find run
scanner.onTag = { tag -> finder.onTag(tag) }
scanner.startScan()

// every 250 ms (Handler.postDelayed):
val signal = finder.signal()
text = when {
    signal != null -> TagFinder.label(signal)              // "Close"
    finder.seen -> "Signal lost"
    else -> "Searching"
}
bars = signal?.let { maxOf(1, (it + 9) / 10) } ?: 0       // 0..10 bars
```

Stop with `scanner.stopScan(); scanner.onTag = null` when the screen closes.

## Tuning (constructor, all optional)

| Knob | Default | Meaning |
|---|---|---|
| `lostAfterMs` | `3000` | no matching read this long → `signal()` is `null` |
| `weakestRssi` | `-85.0` | this dBm or weaker = 0 (weakest C72 read at ~5 m) |
| `strongestRssi` | `-40.0` | this dBm or stronger = 100 (tag against the reader) |

Range also depends on `scanner.power` (rfid-scan). Signal is smoothed (60 % old, 40 % new) so bars don't jump.

## Why it behaves like this

- RSSI is not a distance: the words are relative proximity, never metres.
- Other tags never move the signal; EPC values match only the EPC bank, TID values only the TID bank (case and spaces ignored).
- Far away the TID read often fails while the EPC still arrives. Once a read matched by TID, the EPC that came with it
  keeps matching for the rest of that run. A new `TagFinder` starts clean.

Tests: `./gradlew :rfid-find:test`.
