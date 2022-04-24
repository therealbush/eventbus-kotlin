import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    `maven-publish`
    kotlin("jvm") version "1.6.20"
    id("org.jetbrains.dokka") version "1.6.20"
}

group = "me.bush"
version = "1.0.0"

repositories.mavenCentral()

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.apache.logging.log4j:log4j-api:2.15.0")
    implementation("org.apache.logging.log4j:log4j-core:2.15.0")

    // Additional Kotlin libraries required for some features
    implementation(kotlin("reflect", "1.6.20"))
    implementation(kotlin("coroutines-core", "1.6.1"))
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks {
    test {
        testLogging.showStandardStreams = true
        useJUnitPlatform()
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
    named<Jar>("javadocJar") {
        from(named("dokkaJavadoc"))
    }
}

publishing.publications.create<MavenPublication>("maven").from(components["java"])
