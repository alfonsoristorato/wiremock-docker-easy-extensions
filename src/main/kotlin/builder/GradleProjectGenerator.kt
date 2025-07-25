package builder

import config.Config
import java.io.File


class GradleProjectGenerator {
    /**
     * Generates a temporary Gradle project for compiling a JAR.
     */
    fun generate(buildDir: File, config: Config) {
        buildDir.resolve("settings.gradle.kts").writeText("rootProject.name = \"wiremock-temp-build\"")

        val buildFileContent = """
            import org.jetbrains.kotlin.gradle.dsl.JvmTarget
            plugins {
                java
                kotlin("jvm") version "2.1.20"
                id("com.github.johnrengelman.shadow") version "8.1.1"
            }
            sourceSets {
                main {
                    java {
                        srcDirs("src/main/java", "src/main/kotlin","config/example")
                    }
                    kotlin {
                        srcDirs("src/main", "config/example")
                    }
                    resources {
                        srcDirs("config/resources")
                    }
                }
            }
            java.sourceCompatibility = JavaVersion.VERSION_11
            java.targetCompatibility = JavaVersion.VERSION_11
            kotlin.compilerOptions.jvmTarget = JvmTarget.JVM_11
            repositories { mavenCentral() }

            dependencies {
                implementation("org.wiremock:wiremock-standalone:3.7.0")
                ${config.dependencies?.joinToString("\n                ") { "implementation(\"$it\")" }}
            }

            tasks.shadowJar {
                archiveBaseName.set("extensions-bundled")
                archiveClassifier.set("")
                archiveVersion.set("")
                mergeServiceFiles()
            }
        """.trimIndent()

        buildDir.resolve("build.gradle.kts").writeText(buildFileContent)
    }
}