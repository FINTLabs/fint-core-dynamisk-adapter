plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.spring") version "2.2.0"
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "no.fintlabs"
description = "core-adapter"
val fintVersion: String by project

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven("https://repo.fintlabs.no/releases")
}

dependencies {
    implementation(project(":core-lib"))

    // SPRING
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // FINT
    implementation("no.novari:fint-core-metamodel:3.0.0")
    implementation("no.novari:fint-arkiv-resource-model-java:$fintVersion")
    implementation("no.novari:fint-felles-resource-model-java:$fintVersion")
    implementation("no.novari:fint-ressurs-resource-model-java:$fintVersion")
    implementation("no.novari:fint-okonomi-resource-model-java:$fintVersion")
    implementation("no.novari:fint-ressurs-resource-model-java:$fintVersion")
    implementation("no.novari:fint-utdanning-resource-model-java:$fintVersion")
    implementation("no.novari:fint-personvern-resource-model-java:$fintVersion")
    implementation("no.novari:fint-administrasjon-resource-model-java:$fintVersion")
    implementation("no.fintlabs:fint-core-adapter-common:0.1.6-rc-7")
    implementation("no.fintlabs:fint-core-infra-models:2.1.2")

    // TEST
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.projectreactor:reactor-test")

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
