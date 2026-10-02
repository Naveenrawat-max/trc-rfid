// rfid-find: hot/cold finding of one tag from rfid-scan reads. See README.md.
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    `maven-publish`
}

android {
    namespace = "trc.rfid.find"
    compileSdk = 34
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    publishing { singleVariant("release") }
}

dependencies {
    api(project(":rfid-scan"))
    testImplementation("junit:junit:4.13.2")
}

// Same as rfid-scan: apps resolve through the POMs, which carry the chainway-sdk dependency.
tasks.withType<GenerateModuleMetadata> { enabled = false }

publishing {
    publications {
        register<MavenPublication>("release") {
            artifactId = "rfid-find"
            afterEvaluate { from(components["release"]) }
        }
    }
}
