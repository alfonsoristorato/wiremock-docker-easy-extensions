import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version libs.versions.kotlin
    kotlin("plugin.serialization") version libs.versions.kotlin
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

kover {
    reports {
        verify {
            rule {
                minBound(50)
            }

//            rule("Minimal line coverage in percents") {
//                bound {
//                    minValue = 50
//                    coverageUnits = CoverageUnit.LINE
//                    aggregationForGroup = AggregationType.COVERED_PERCENTAGE
//                }
//            }
        }
    }
}
