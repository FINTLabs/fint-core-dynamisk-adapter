plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.spring") version "2.2.0"
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "no.fintlabs"
version = "1.0.0"
description = "fint-core-dynamisk-adapter-core-api"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    maven("https://repo.fintlabs.no/releases")
    mavenCentral()
}

val fintVersion = "3.21.10"

dependencies {
    implementation(project(":core-lib"))
    implementation(project(":core-contract"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.1")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.2.2")

    implementation("no.fintlabs:fint-core-consumer-metamodel:2.0.0-rc-4")
    implementation("no.fint:fint-utdanning-resource-model-java:$fintVersion")
    implementation("no.fint:fint-administrasjon-resource-model-java:$fintVersion")
    implementation("no.fint:fint-personvern-resource-model-java:$fintVersion")
    implementation("no.fint:fint-okonomi-resource-model-java:$fintVersion")
    implementation("no.fint:fint-ressurs-resource-model-java:$fintVersion")
    implementation("no.fint:fint-arkiv-resource-model-java:$fintVersion")

    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
