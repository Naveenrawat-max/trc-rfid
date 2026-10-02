---
title: "rfid-scan/src/main/java/trc/rfid/scan/RfidScanner.kt"
source: "rfid-scan/src/main/java/trc/rfid/scan/RfidScanner.kt"
language: kotlin
lines: 155
imports: 0
imported_by: 0
tags:
  - kind/code
  - lang/kotlin
  - role/isolated
  - area/rfid-scan-src-main-java-trc-rfid-scan
---

<!-- vaultify:auto:start -->
# `rfid-scan/src/main/java/trc/rfid/scan/RfidScanner.kt`

> Chainway UHF reader (C72 and other UART models): connect, scan EPC+TID continuously, read one tag, read a memory bank. */

## Imports

_no internal imports_

## Imported by

_nothing in this project imports it_

## Notes

Part of [[_code/00 Code map]].
<!-- vaultify:auto:end -->
## Why / gotchas

Wraps Chainway's `RFIDWithUHFUART` and hands every read to `onTag` as a [[_code/rfid-scan/src/main/java/trc/rfid/scan/Tag-kt|Tag]] on the main thread. [[_code/rfid-find/src/main/java/trc/rfid/find/TagFinder-kt|TagFinder]] consumes those reads.

- What breaks if you change it: `startScan()` re-applies EPC+TID mode and `power` every time because Chainway's Keyboard Emulator silently changes both; removing that makes Find miss TID-registered tags.
- `readOnce()` / `readMemory()` return null while scanning. The EPC filter in `readMemory` starts at bit 32 (after CRC + PC), and its length is in bits (`epc.length * 4`). Not yet verified on a physical C72.
- Calibration knob: `power` (default 30 dBm, ~5 m on a C72).
- Published with the Chainway SDK as a separate artifact: [[_meta/decisions/001-jitpack-with-bundled-chainway-sdk]].
