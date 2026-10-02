---
title: "rfid-find/src/main/java/trc/rfid/find/TagFinder.kt"
source: "rfid-find/src/main/java/trc/rfid/find/TagFinder.kt"
language: kotlin
lines: 92
imports: 0
imported_by: 0
tags:
  - kind/code
  - lang/kotlin
  - role/isolated
  - area/rfid-find-src-main-java-trc-rfid-find
---

<!-- vaultify:auto:start -->
# `rfid-find/src/main/java/trc/rfid/find/TagFinder.kt`

> Hot/cold finding of one tag: matches scanner reads to the target and turns RSSI into a 0-100 signal and a word. */

## Imports

_no internal imports_

## Imported by

_nothing in this project imports it_

## Notes

Part of [[_code/00 Code map]].
<!-- vaultify:auto:end -->
## Why / gotchas

Imports [[_code/rfid-scan/src/main/java/trc/rfid/scan/Tag-kt|Tag]] from rfid-scan, so `rfid-find` depends on `rfid-scan` (`api(project(":rfid-scan"))`). Feed it reads from [[_code/rfid-scan/src/main/java/trc/rfid/scan/RfidScanner-kt|RfidScanner]].`onTag`.

- What breaks if you change it: the smoothing (0.6 old / 0.4 new), the 3 s `lostAfterMs` and the RSSI range (-85…-40 dBm) were tuned on a C72. Change them per device through the constructor, not in the code.
- TID→EPC pairing: after a TID match, that read's EPC keeps matching, because at range the TID read fails first. Tags sharing a factory-default EPC would also count.
- Behaviour pinned by [[_code/rfid-find/src/test/java/trc/rfid/find/TagFinderTest-kt|TagFinderTest]].
