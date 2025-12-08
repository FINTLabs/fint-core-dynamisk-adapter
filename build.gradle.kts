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

val fintVersion = "3.21.10"

dependencies {

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.1")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.2.2")

    // FINT
    implementation("no.fintlabs:fint-core-consumer-metamodel:2.0.0-rc-4")
    implementation("no.fint:fint-utdanning-resource-model-java:$fintVersion")
    implementation("no.fint:fint-administrasjon-resource-model-java:$fintVersion")
    implementation("no.fint:fint-personvern-resource-model-java:$fintVersion")
    implementation("no.fint:fint-okonomi-resource-model-java:$fintVersion")
    implementation("no.fint:fint-ressurs-resource-model-java:$fintVersion")
    implementation("no.fint:fint-arkiv-resource-model-java:$fintVersion")
}