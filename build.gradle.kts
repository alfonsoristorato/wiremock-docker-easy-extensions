import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.shadow.jar)
    alias(libs.plugins.gradle.ktlint)
    alias(libs.plugins.kover)
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
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.mockk)
    testImplementation(libs.testcontainers)
}

tasks.build {
    dependsOn(tasks.ktlintFormat, tasks.koverVerify, tasks.shadowJar)
}

tasks.jar {
    enabled = false
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    compilerOptions {
        extraWarnings.set(true)
        allWarningsAsErrors.set(true)
    }
}

java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11
kotlin.compilerOptions.jvmTarget = JvmTarget.JVM_11

tasks.shadowJar {
    enableAutoRelocation = true
    archiveBaseName = "wiremock-docker-easy-extensions"
    archiveClassifier = ""
    archiveVersion = ""
    manifest {
        attributes["Main-Class"] = "wdee.WireMockDockerEasyExtensions"
    }
    mergeServiceFiles()
}

tasks.processResources {
    from(layout.projectDirectory) {
        include("gradlew", "gradlew.bat")
        into("")
    }
    from(layout.projectDirectory.dir("gradle")) {
        into("/gradle")
    }
}

tasks.processTestResources {
    from(layout.projectDirectory.dir("examples")) {
        include(
            "__files/**",
            "extensions/**",
            "mappings/**",
            "wdee-config.yaml",
        )
        into("/e2e-resources")
    }
}

kover {
    reports {
        verify {
            rule {
                minBound(90)
            }
        }
    }
}
