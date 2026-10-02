// rfid-scan: Chainway UHF reader (scan, read one tag, read a memory bank). See README.md.
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    `maven-publish`
}

android {
    namespace = "trc.rfid.scan"
    compileSdk = 34
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    publishing { singleVariant("release") }
}

// A library AAR cannot contain another local AAR. So the Chainway SDK is compile-only here and published next to
// rfid-scan as its own artifact (chainway-sdk); rfid-scan's POM depends on it, so apps get it automatically.
val chainway = file("libs/DeviceAPI_ver20251103_release.aar")

dependencies {
    compileOnly(files(chainway))
    testImplementation(files(chainway))
    testImplementation("junit:junit:4.13.2")
}

// The extra chainway-sdk dependency is only written into the POM; Gradle module metadata would hide it.
tasks.withType<GenerateModuleMetadata> { enabled = false }

publishing {
    publications {
        register<MavenPublication>("release") {
            artifactId = "rfid-scan"
            afterEvaluate { from(components["release"]) }
            pom.withXml {
                val root = asNode()
                val deps = root.children().filterIsInstance<groovy.util.Node>()
                    .firstOrNull { it.name().toString().endsWith("dependencies") }
                    ?: root.appendNode("dependencies")
                deps.appendNode("dependency").apply {
                    appendNode("groupId", project.group)
                    appendNode("artifactId", "chainway-sdk")
                    appendNode("version", project.version)
                    appendNode("type", "aar")
                    appendNode("scope", "compile")
                }
            }
        }
        register<MavenPublication>("chainway") {
            artifactId = "chainway-sdk"
            artifact(chainway) { extension = "aar" }
            pom { packaging = "aar" }
        }
    }
}
