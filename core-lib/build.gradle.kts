plugins {
    kotlin("jvm") version "2.2.0"
    `maven-publish`
    `java-library`
}

kotlin {
    jvmToolchain(21)
}

group = "no.fintlabs"
val version: String by project

java {
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
    maven("https://repo.fintlabs.no/releases")
}


dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // FINT resource/model dependencies
    implementation("no.fintlabs:fint-core-consumer-metamodel:2.0.0-rc-4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.1")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.2.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.0")

    implementation("no.fintlabs:fint-core-consumer-metamodel:2.0.0-rc-4")
    implementation("no.fint:fint-utdanning-resource-model-java:$version")
    implementation("no.fint:fint-administrasjon-resource-model-java:$version")
    implementation("no.fint:fint-personvern-resource-model-java:$version")
    implementation("no.fint:fint-okonomi-resource-model-java:$version")
    implementation("no.fint:fint-ressurs-resource-model-java:$version")
    implementation("no.fint:fint-arkiv-resource-model-java:$version")
}



configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.1")
    }
}