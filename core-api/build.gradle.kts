plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.spring") version "2.2.0"
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "no.fintlabs"
description = "core-api"
val fintVersion: String by project

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    maven("https://repo.fintlabs.no/releases")
    mavenCentral()
}

dependencies {
    implementation(project(":core-contract"))
    implementation(project(":core-runtime"))

    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("no.fintlabs:fint-core-consumer-metamodel:2.0.0-rc-4")
    implementation("no.fint:fint-utdanning-resource-model-java:$fintVersion")
    implementation("no.fint:fint-administrasjon-resource-model-java:$fintVersion")
    implementation("no.fint:fint-personvern-resource-model-java:$fintVersion")
    implementation("no.fint:fint-okonomi-resource-model-java:$fintVersion")
    implementation("no.fint:fint-ressurs-resource-model-java:$fintVersion")
    implementation("no.fint:fint-arkiv-resource-model-java:$fintVersion")

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
