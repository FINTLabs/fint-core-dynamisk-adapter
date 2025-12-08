plugins {
    kotlin("jvm") version "2.2.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
}

repositories {
    mavenCentral()
    maven("https://repo.fintlabs.no/releases")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

val fintVersion = "3.21.10"

dependencies {
    implementation(project(":core-contract"))

    implementation("no.fintlabs:fint-core-consumer-metamodel:2.0.0-rc-4")
    implementation("no.fint:fint-utdanning-resource-model-java:$fintVersion")
    implementation("no.fint:fint-administrasjon-resource-model-java:$fintVersion")
    implementation("no.fint:fint-personvern-resource-model-java:$fintVersion")
    implementation("no.fint:fint-okonomi-resource-model-java:$fintVersion")
    implementation("no.fint:fint-ressurs-resource-model-java:$fintVersion")
    implementation("no.fint:fint-arkiv-resource-model-java:$fintVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.5")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("org.jetbrains.skiko:skiko-awt-runtime-macos-arm64:0.8.0")
    implementation("org.jetbrains.compose.ui:ui-desktop:1.6.11")
    implementation("org.jetbrains.compose.foundation:foundation-desktop:1.6.11")
    implementation("org.jetbrains.compose.material:material-desktop:1.6.11")
    implementation("org.jetbrains.compose.runtime:runtime-desktop:1.6.11")
}

kotlin {
    jvmToolchain(21)
}