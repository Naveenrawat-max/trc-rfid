---
title: "rfid-find/src/test/java/trc/rfid/find/TagFinderTest.kt"
source: "rfid-find/src/test/java/trc/rfid/find/TagFinderTest.kt"
language: kotlin
lines: 90
imports: 0
imported_by: 0
tags:
  - kind/code
  - lang/kotlin
  - role/isolated
  - area/rfid-find-src-test-java-trc-rfid-find
---

<!-- vaultify:auto:start -->
# `rfid-find/src/test/java/trc/rfid/find/TagFinderTest.kt`

## Imports

_no internal imports_

## Imported by

_nothing in this project imports it_

## Notes

Part of [[_code/00 Code map]].
<!-- vaultify:auto:end -->
## Why / gotchas

Pins the tuned behaviour of [[_code/rfid-find/src/main/java/trc/rfid/find/TagFinder-kt|TagFinder]]: scaling, labels, smoothing, timeout, bank matching, TID→EPC pairing. Run: `./gradlew :rfid-find:test`.
