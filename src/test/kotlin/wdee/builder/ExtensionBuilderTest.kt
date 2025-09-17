package wdee.builder

import io.kotest.core.spec.style.StringSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import utils.TestUtils
import java.io.File

class ExtensionBuilderTest :
    StringSpec({

        val tempDir = tempdir()
        val gradleProjectGenerator = GradleProjectGenerator()
        val processBuilder = mockk<ProcessBuilder>(relaxed = true)
        val extensionBuilder =
            ExtensionBuilder(gradleProjectGenerator = gradleProjectGenerator, processBuilder = processBuilder)

        beforeAny {
            TestUtils.mockContextHolder(
                projectRoot = tempDir,
                sourceFilesLocation = "",
                sourceFiles = listOf("com.example.MyExtension.kt"),
            )
        }

        afterAny {
            clearAllMocks()
        }

        fun generateRequiredFiles(
            libsVersionFilePresent: Boolean,
            sourceFilePresent: Boolean,
            gradleWrapperPresent: Boolean,
        ) {
            if (libsVersionFilePresent) {
                val gradleDir = File(tempDir, "gradle")
                gradleDir.mkdirs()
                File(gradleDir, "libs.versions.toml").writeText("s")
            }
            if (sourceFilePresent) {
                val sourceFile = File(tempDir, "MyExtension.kt")
                sourceFile.writeText("")
            }
            if (gradleWrapperPresent) {
                val gradleWrapper = File(tempDir, "gradlew")
                gradleWrapper.writeText("")
                gradleWrapper.setExecutable(true)
                val gradleWrapperBat = File(tempDir, "gradlew.bat")
                gradleWrapperBat.writeText("")
            }
        }

        fun simulateFinalJarCreation(tempDir: File) {
            val libsDir = File(tempDir, ".extensions-builder/build/libs")
            libsDir.mkdirs()
            File(libsDir, "extensions-bundled.jar").createNewFile()
        }

        "build should succeed when gradle build is successful" {
            generateRequiredFiles(libsVersionFilePresent = true, sourceFilePresent = true, gradleWrapperPresent = true)
            val mockProcess = mockk<Process>(relaxed = true)
            every { processBuilder.command(any<List<String>>()) } returns processBuilder
            every { processBuilder.directory(any()) } returns processBuilder
            every { processBuilder.inheritIO() } returns processBuilder
            every { processBuilder.start() } answers {
                simulateFinalJarCreation(tempDir)
                mockProcess
            }
            every { mockProcess.waitFor() } returns 0
            val result = extensionBuilder.build()

            result shouldBe true
        }
    })
