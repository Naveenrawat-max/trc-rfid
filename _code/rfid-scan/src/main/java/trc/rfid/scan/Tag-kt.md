---
title: "rfid-scan/src/main/java/trc/rfid/scan/Tag.kt"
source: "rfid-scan/src/main/java/trc/rfid/scan/Tag.kt"
language: kotlin
lines: 14
imports: 0
imported_by: 0
tags:
  - kind/code
  - lang/kotlin
  - role/isolated
  - area/rfid-scan-src-main-java-trc-rfid-scan
---

<!-- vaultify:auto:start -->
# `rfid-scan/src/main/java/trc/rfid/scan/Tag.kt`

> One tag read by the scanner: EPC, TID and signal strength. */

## Imports

_no internal imports_

## Imported by

_nothing in this project imports it_

## Notes

Part of [[_code/00 Code map]].
<!-- vaultify:auto:end -->
## Why / gotchas

The one data type shared by both packages: produced by [[_code/rfid-scan/src/main/java/trc/rfid/scan/RfidScanner-kt|RfidScanner]], matched by [[_code/rfid-find/src/main/java/trc/rfid/find/TagFinder-kt|TagFinder]]. Empty string = bank not read (never null), so callers don't need null checks. `rssiFromText` exists because the SDK sometimes reports `-62,5`; checked by [[_code/rfid-scan/src/test/java/trc/rfid/scan/TagTest-kt|TagTest]].
