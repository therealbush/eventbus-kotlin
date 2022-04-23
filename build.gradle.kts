import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("maven-publish")
    kotlin("jvm") version "1.6.20"
    id("org.jetbrains.dokka") version "1.6.20"
}

group = "me.bush"
version = "1.0.0"

repositories.mavenCentral()

dependencies {
    testImplementation(kotlin("test"))

    // 2.15.0 is not vulnerable to any RCE exploits.
    implementation("org.apache.logging.log4j:log4j-api:2.15.0")
    implementation("org.apache.logging.log4j:log4j-core:2.15.0")

    // Additional Kotlin libraries required for some features
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.test {
    testLogging.showStandardStreams = true
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val javadocJar = tasks.named<Jar>("javadocJar") {
    from(tasks.named("dokkaJavadoc"))
}

publishing.publications.create<MavenPublication>("maven").from(components["java"])
