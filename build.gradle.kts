plugins {
    kotlin("jvm") version "2.2.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
}

group = "no.fintlabs"
version = "1.0.0"
description = "fint-core-dynamisk-adapter"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    maven("https://repo.fintlabs.no/releases")
}

val fintVersion = "4.0.10"

dependencies {

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.1")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.2.2")

    // FINT
    implementation("no.novari:fint-core-metamodel:3.0.0")
    implementation("no.novari:fint-arkiv-resource-model-java:$fintVersion")
    implementation("no.novari:fint-felles-resource-model-java:$fintVersion")
    implementation("no.novari:fint-okonomi-resource-model-java:$fintVersion")
    implementation("no.novari:fint-ressurs-resource-model-java:$fintVersion")
    implementation("no.novari:fint-utdanning-resource-model-java:$fintVersion")
    implementation("no.novari:fint-personvern-resource-model-java:$fintVersion")
    implementation("no.novari:fint-administrasjon-resource-model-java:$fintVersion")
}
