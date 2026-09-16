plugins {
    kotlin("jvm") version "2.4.20"
    kotlin("plugin.spring") version "2.4.20"
}

group = "no.fintlabs"
description = "core-runtime"
val fintVersion: String by project

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
    maven("https://repo.fintlabs.no/releases")
}

dependencies {
    implementation(project(":core-contract"))
    implementation(project(":core-adapter"))
    implementation(project(":core-engine"))

    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.3.3"))

    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("javax.validation:validation-api:2.0.1.Final")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("no.novari:fint-core-metamodel:3.0.0")
    implementation("no.novari:fint-arkiv-resource-model-java:$fintVersion")
    implementation("no.novari:fint-felles-resource-model-java:$fintVersion")
    implementation("no.novari:fint-ressurs-resource-model-java:$fintVersion")
    implementation("no.novari:fint-okonomi-resource-model-java:$fintVersion")
    implementation("no.novari:fint-ressurs-resource-model-java:$fintVersion")
    implementation("no.novari:fint-utdanning-resource-model-java:$fintVersion")
    implementation("no.novari:fint-personvern-resource-model-java:$fintVersion")
    implementation("no.novari:fint-administrasjon-resource-model-java:$fintVersion")
    implementation("no.fintlabs:fint-core-infra-models:2.1.2")

    implementation("org.springframework.boot:spring-boot-starter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
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
