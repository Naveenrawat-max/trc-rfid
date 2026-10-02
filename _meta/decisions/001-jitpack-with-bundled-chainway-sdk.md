---
title: "001 Publish through JitPack, Chainway SDK as its own artifact"
tags:
  - kind/decision
  - area/publishing
  - status/current
---

# 001 Publish through JitPack, Chainway SDK as its own artifact

**Context.** The packages must be importable in new apps with one version string, like `go get`. Gradle only downloads built artifacts from Maven repositories, not source from GitHub. [[_code/rfid-scan/src/main/java/trc/rfid/scan/RfidScanner-kt|RfidScanner]] needs Chainway's proprietary `DeviceAPI_ver20251103_release.aar`, and a library AAR cannot contain another local AAR.

**Options.** JitPack (builds tags from a public GitHub repo, free), GitHub Packages (a token is needed even to read), Maven Central (signing and namespace verification), `includeBuild` from a clone (private, but no version string).

**Choice (2026-10-02, owner's call).** Public GitHub repo `Naveenrawat-max/trc-rfid` with JitPack. The SDK stays `compileOnly` in rfid-scan and is published beside it as `chainway-sdk`. The rfid-scan POM gets an extra dependency on it (`pom.withXml`), and Gradle module metadata is disabled so consumers read that POM. Tags are plain `1.0.0`, not `v1.0.0`, so the import reads `...:rfid-scan:1.0.0`.

**Consequences.**
- Apps add `maven("https://jitpack.io")` once plus one `implementation` line; the SDK and its native libs arrive automatically (verified with a throwaway consumer app against `publishToMavenLocal`).
- The Chainway SDK is publicly downloadable from GitHub and JitPack: licence risk accepted by the owner.
- Module metadata off means no Gradle variant-aware features; fine for plain AARs.
- Group/version come from root `build.gradle.kts`; JitPack's `VERSION` env var sets the version to the tag.
