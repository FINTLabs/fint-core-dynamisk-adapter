plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.spring") version "2.0.0"
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.5"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
}

group = "no.fintlabs"
version = "0.0.1-SNAPSHOT"
description = "fint-core-dynamisk-adapter"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://repo.fintlabs.no/releases")
    google()
}

val fintVersion = "3.19.0"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    implementation("no.fint:fint-utdanning-model-java:$fintVersion")
    implementation("no.fint:fint-utdanning-resource-model-java:$fintVersion")

    implementation("io.github.serpro69:kotlin-faker:1.14.0") {
        exclude(group = "com.fasterxml.jackson.module", module = "jackson-module-kotlin")
    }

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.apache.kafka:kafka-streams")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.apache.kafka:kafka-clients")
    implementation("org.testcontainers:kafka")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("no.fintlabs:fint-kafka:3.0.0-rc-1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.5")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("org.jetbrains.skiko:skiko-awt-runtime-macos-arm64:0.8.0")
    implementation("org.jetbrains.compose.ui:ui-desktop:1.6.11")
    implementation("org.jetbrains.compose.foundation:foundation-desktop:1.6.11")
    implementation("org.jetbrains.compose.material:material-desktop:1.6.11")
    implementation("org.jetbrains.compose.runtime:runtime-desktop:1.6.11")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
