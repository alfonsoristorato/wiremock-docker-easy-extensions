package builder

import config.Config
import java.io.File

class GradleProjectGenerator {
    /**
     * Generates a temporary Gradle project for compiling a JAR.
     */
    fun generate(
        buildDir: File,
        config: Config,
        libsVersionsToml: File,
    ) {
        val gradleDir = buildDir.resolve("gradle")
        gradleDir.mkdirs()
        libsVersionsToml.copyTo(gradleDir.resolve("libs.versions.toml"))
        buildDir.resolve("settings.gradle.kts").writeText("rootProject.name = \"extensions-builder-temp\"")

        val buildFileContent =
            """
            import org.jetbrains.kotlin.gradle.dsl.JvmTarget
            plugins {
                java
                kotlin("jvm") version libs.versions.kotlin
                alias(libs.plugins.shadow.jar)
            }
            sourceSets {
                main {
                    java {
                        srcDirs("${config.filesLocation}")
                    }
                    kotlin {
                        srcDirs("${config.filesLocation}")
                    }
                    resources {
                        srcDirs("${config.filesLocation}/resources")
                    }
                }
            }
            java.sourceCompatibility = JavaVersion.VERSION_11
            java.targetCompatibility = JavaVersion.VERSION_11
            kotlin.compilerOptions.jvmTarget = JvmTarget.JVM_11
            repositories { mavenCentral() }

            dependencies {
                implementation(libs.wiremock)
                ${config.dependencies?.joinToString("\n                ") { "implementation(\"$it\")" }}
            }

            tasks.shadowJar {
                archiveBaseName = "extensions-bundled"
                archiveClassifier = ""
                archiveVersion = ""
                mergeServiceFiles()
            }
            """.trimIndent()

        buildDir.resolve("build.gradle.kts").writeText(buildFileContent)
    }
}
