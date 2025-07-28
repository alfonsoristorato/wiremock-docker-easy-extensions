import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version libs.versions.kotlin
    kotlin("plugin.serialization") version libs.versions.kotlin
    alias(libs.plugins.shadow.jar)
    alias(libs.plugins.gradle.ktlint)
    alias(libs.plugins.kover)
    `maven-publish`
}

group = "alfonsoristorato"
//TODO align this version with gh releases
version = project.findProperty("projectVersion") ?: "1.0.0"

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
    testImplementation(libs.bundles.kotest)
}

tasks.build {
    dependsOn(tasks.ktlintFormat, tasks.shadowJar)
}

tasks.jar {
    enabled = false
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.koverVerify)
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

publishing {
    publications {
        create<MavenPublication>("shadow") {
            from(components["shadow"])
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/alfonsoristorato/wiremock-docker-easy-extensions")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

kover {
    reports {
        verify {
            rule {
                minBound(0) // TODO replace with actual minimum coverage percentage once there are tests
            }
            rule("Class coverage") {
                minBound(0) // TODO replace with actual minimum coverage percentage once there are tests
            }
        }
    }
}
