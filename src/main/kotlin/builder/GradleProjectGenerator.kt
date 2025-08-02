package builder

import config.ContextHolder
import utils.Utils
import java.io.File

class GradleProjectGenerator {
    /**
     * Generates a temporary Gradle project for compiling a JAR.
     * @param buildDir The directory where the Gradle project will be created.
     */
    fun generate(buildDir: File) {
        val gradleDir = buildDir.resolve(ContextHolder.GradleConfig.GRADLE_DIR)
        gradleDir.mkdirs()

        Utils.ResourceUtils.copyResourceToFile(
            "/${ContextHolder.GradleConfig.GRADLE_DIR}/${ContextHolder.GradleConfig.LIBS_FILE_NAME}",
            gradleDir.resolve(ContextHolder.GradleConfig.LIBS_FILE_NAME),
        )

        buildDir
            .resolve(ContextHolder.GradleConfig.SETTINGS_GRADLE_FILE_NAME)
            .writeText("rootProject.name = \"extensions-builder-temp\"")

        val buildFileContent =
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
                ${ContextHolder.SourceFilesConfig.dependencies.joinToString("\n                ") { "implementation(\"$it\")" }}
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

        buildDir.resolve(ContextHolder.GradleConfig.BUILD_GRADLE_FILE_NAME).writeText(buildFileContent)
    }
}
