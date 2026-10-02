# trc-rfid

Reusable Android packages for Chainway UHF handhelds (C72 and other UART models):

| Package | Does | Docs |
|---|---|---|
| `rfid-scan` | scan tags (EPC, TID, RSSI), read one tag, read any memory bank | [rfid-scan/README.md](rfid-scan/README.md) |
| `rfid-find` | hot/cold finding of one tag: 0-100 signal + *Very far … Very close* | [rfid-find/README.md](rfid-find/README.md) |

The Chainway SDK (`DeviceAPI_ver20251103_release.aar`) comes along automatically; nothing to copy.

## Use in a new app

1. `settings.gradle.kts`: add JitPack (once per app):

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

2. `app/build.gradle.kts`: add one line:

```kotlin
dependencies {
    implementation("com.github.Naveenrawat-max.trc-rfid:rfid-find:1.0.0") // scan + find
    // or only scanning:
    // implementation("com.github.Naveenrawat-max.trc-rfid:rfid-scan:1.0.0")
}
```

3. Sync Gradle, then:

```kotlin
import trc.rfid.scan.RfidScanner
import trc.rfid.find.TagFinder

val scanner = RfidScanner(context)
thread { scanner.connect() }
scanner.onTag = { tag -> println("${tag.epc} ${tag.tid} ${tag.rssi}") }
scanner.startScan()
```

Requirements: minSdk 26, Java 17. Optional: `defaultConfig { ndk { abiFilters += listOf("armeabi-v7a", "arm64-v8a") } }`
drops the SDK's obsolete `armeabi` libraries from your APK.

Newer version: change `1.0.0` to the new tag and sync.

## Release a new version

```bash
./gradlew build                      # tests + lint must pass
git tag 1.1.0 && git push origin 1.1.0
```

The first app that asks for `1.1.0` makes JitPack build it (a few minutes; log at `https://jitpack.io/#Naveenrawat-max/trc-rfid`).
Any branch or commit also works as a version: `main-SNAPSHOT`, or a short commit hash.

## Develop

```bash
./gradlew build                 # compile, unit tests, lint for both packages
./gradlew publishToMavenLocal   # try a change in an app before tagging: add mavenLocal() to that app's repositories
```

`local.properties` needs `sdk.dir=...` (Android Studio writes it). JitPack builds with JDK 17 (`jitpack.yml`).

## Licence note

`rfid-scan/libs/DeviceAPI_ver20251103_release.aar` is Chainway's proprietary SDK, redistributed here and published as
`chainway-sdk`. Use it only with Chainway devices, under Chainway's terms.
