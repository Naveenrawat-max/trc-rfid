// trc-rfid: reusable Chainway UHF packages, published through JitPack (see README.md).
plugins {
    id("com.android.library") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
}

subprojects {
    // JitPack serves multi-module repos as com.github.<user>.<repo>:<module>:<git tag>, and sets VERSION to that tag.
    group = "com.github.Naveenrawat-max.trc-rfid"
    version = System.getenv("VERSION") ?: "1.0.0"
}
