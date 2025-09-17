package wdee.builder

import io.kotest.core.spec.style.StringSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import utils.TestUtils

class GradleProjectGeneratorTest :
    StringSpec({

        val tempDir = tempdir()
        val generator = GradleProjectGenerator()
        afterAny {
            clearAllMocks()
            tempDir.deleteRecursively()
        }

        "should generate gradle project files with the build.gradle including as many dependencies as provided" {

            val dependencies = listOf("com.example:dependency1:1.0", "com.example:dependency2:2.0")
            TestUtils.mockContextHolder(dependencies = dependencies)

            generator.generate(tempDir)

            val gradleDir = tempDir.resolve("gradle")
            gradleDir.exists() shouldBe true

            val libsFile = gradleDir.resolve("libs.versions.toml")
            libsFile.exists() shouldBe true

            val settingsFile = tempDir.resolve("settings.gradle.kts")
            settingsFile.exists() shouldBe true
            settingsFile.readText() shouldBe "rootProject.name = \"extensions-builder-temp\""

            val buildFile = tempDir.resolve("build.gradle.kts")
            buildFile.exists() shouldBe true
            val expectedBuildFileContent =
                """
                import org.jetbrains.kotlin.gradle.dsl.JvmTarget
                plugins {
                    kotlin("jvm") version libs.versions.kotlin
                    alias(libs.plugins.shadow.jar)
                }

                java.sourceCompatibility = JavaVersion.VERSION_11
                java.targetCompatibility = JavaVersion.VERSION_11
                kotlin.compilerOptions.jvmTarget = JvmTarget.JVM_11
                repositories { mavenCentral() }

                dependencies {
                    implementation(libs.wiremock)
                    implementation("com.example:dependency1:1.0")
                    implementation("com.example:dependency2:2.0")
                }
                
                tasks.jar {
                    enabled = false
                }
                
                tasks.shadowJar {
                    archiveBaseName = "extensions-bundled"
                    archiveClassifier = ""
                    archiveVersion = ""
                    mergeServiceFiles()
                }
                """.trimIndent()
            buildFile.readText() shouldBe expectedBuildFileContent
        }
    })
