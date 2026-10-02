---
title: "rfid-scan/src/test/java/trc/rfid/scan/TagTest.kt"
source: "rfid-scan/src/test/java/trc/rfid/scan/TagTest.kt"
language: kotlin
lines: 22
imports: 0
imported_by: 0
tags:
  - kind/code
  - lang/kotlin
  - role/isolated
  - area/rfid-scan-src-test-java-trc-rfid-scan
---

<!-- vaultify:auto:start -->
# `rfid-scan/src/test/java/trc/rfid/scan/TagTest.kt`

## Imports

_no internal imports_

## Imported by

_nothing in this project imports it_

## Notes

Part of [[_code/00 Code map]].
<!-- vaultify:auto:end -->
## Why / gotchas

Covers RSSI parsing in [[_code/rfid-scan/src/main/java/trc/rfid/scan/Tag-kt|Tag]] and the trigger keys of [[_code/rfid-scan/src/main/java/trc/rfid/scan/RfidScanner-kt|RfidScanner]]. Run: `./gradlew :rfid-scan:test`. The radio itself cannot be unit-tested; it needs a C72.
