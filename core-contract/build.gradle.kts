plugins {
    kotlin("jvm") version "2.2.0"
}

kotlin {
    jvmToolchain(21)
}

group = "no.fintlabs"
description = "core-contract"
val fintVersion: String by project

repositories {
    mavenCentral()
    maven("https://repo.fintlabs.no/releases")
}

dependencies {

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.1")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.2.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.0")

    implementation("no.novari:fint-core-metamodel:3.0.0")
    implementation("no.novari:fint-arkiv-resource-model-java:$fintVersion")
    implementation("no.novari:fint-felles-resource-model-java:$fintVersion")
    implementation("no.novari:fint-ressurs-resource-model-java:$fintVersion")
    implementation("no.novari:fint-okonomi-resource-model-java:$fintVersion")
    implementation("no.novari:fint-ressurs-resource-model-java:$fintVersion")
    implementation("no.novari:fint-utdanning-resource-model-java:$fintVersion")
    implementation("no.novari:fint-personvern-resource-model-java:$fintVersion")
    implementation("no.novari:fint-administrasjon-resource-model-java:$fintVersion")
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.1")
    }
}