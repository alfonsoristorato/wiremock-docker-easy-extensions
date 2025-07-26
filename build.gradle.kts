import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version libs.versions.kotlin
    kotlin("plugin.serialization") version libs.versions.kotlin
    alias(libs.plugins.shadow.jar)
    alias(libs.plugins.gradle.ktlint)
}

group = "alfonsoristorato"

repositories {
    mavenCentral()
}

dependencies {
    // Core dependencies
    implementation(libs.kotlin.stdlib)

    // YAML parsing
    implementation(libs.kaml)
    implementation(libs.kotlinx.serialization.json)

    // Testing
    testImplementation(kotlin("test"))
}

tasks.build {
    dependsOn(tasks.ktlintFormat, tasks.shadowJar)
}

tasks.jar {
    enabled = false
}

tasks.test {
    useJUnitPlatform()
}

java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11
kotlin.compilerOptions.jvmTarget = JvmTarget.JVM_11

tasks.shadowJar {
    enableRelocation = true
    archiveBaseName = "wiremock-extensions-builder"
    archiveClassifier = ""
    archiveVersion = ""
    manifest {
        attributes["Main-Class"] = "app.Application"
    }
    mergeServiceFiles()
}
