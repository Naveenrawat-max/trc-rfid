---
title: "trc-rfid"
tags:
  - kind/map
---

# trc-rfid

Reusable Android packages for Chainway UHF handhelds. Usage and release steps: [[README]].

- [[rfid-scan/README|rfid-scan]]: scanning, reading one tag, memory banks. Code: [[_code/rfid-scan/src/main/java/trc/rfid/scan/RfidScanner-kt|RfidScanner]], [[_code/rfid-scan/src/main/java/trc/rfid/scan/Tag-kt|Tag]]
- [[rfid-find/README|rfid-find]]: hot/cold finding. Code: [[_code/rfid-find/src/main/java/trc/rfid/find/TagFinder-kt|TagFinder]]

## Maps

- [[_code/00 Code map]] — every source file, what it imports, what imports it
- [[_meta/Conventions]] — how notes in this vault are written

## Decisions

- [[_meta/decisions/001-jitpack-with-bundled-chainway-sdk]]: why JitPack, and how the Chainway SDK reaches apps

## Open threads

- `readOnce()` / `readMemory()` not yet verified on a physical C72.
- vaultify does not parse Kotlin imports yet: edges between code notes are hand-written in each note's *Why / gotchas*.
