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
val fintVersion: String by project

java {
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
    maven("https://repo.fintlabs.no/releases")
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.1")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.2.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.0")

    // FINT resource/model dependencies
    implementation("no.novari:fint-core-metamodel:3.0.0")
    implementation("no.novari:fint-arkiv-resource-model-java:$fintVersion")
    implementation("no.novari:fint-felles-resource-model-java:$fintVersion")
    implementation("no.novari:fint-okonomi-resource-model-java:$fintVersion")
    implementation("no.novari:fint-ressurs-resource-model-java:$fintVersion")
    implementation("no.novari:fint-utdanning-resource-model-java:$fintVersion")
    implementation("no.novari:fint-personvern-resource-model-java:$fintVersion")
    implementation("no.novari:fint-administrasjon-resource-model-java:$fintVersion")
}

publishing {
    publications {
        create<MavenPublication>("maven") {

            groupId = "no.fintlabs"
            artifactId = "dynamisk-adapter-core-lib"
            version = project.property("version").toString()

            from(components["kotlin"])

            artifact(tasks.named("sourcesJar"))
            artifact(tasks.named("javadocJar"))
        }
    }

    repositories {
        maven {
            url = uri("https://repo.fintlabs.no/releases")
            credentials {
                username = System.getenv("REPOSILITE_USERNAME")
                password = System.getenv("REPOSILITE_PASSWORD")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.1")
    }
}
