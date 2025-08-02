package builder

import config.ContextHolder
import utils.Utils.OsUtils
import utils.Utils.ResourceUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.createDirectories

class ExtensionBuilder(
    private val gradleGenerator: GradleProjectGenerator = GradleProjectGenerator(),
) {
    /**
     * Builds JAR containing the extension classes, their Service Loader and dependencies according to the provided configuration.
     *
     * @return true if the build was successful, false otherwise
     */
    fun build(): Boolean {
        val tempBuildDir = ContextHolder.projectRoot.resolve(".extensions-builder")

        return runCatching {
            tempBuildDir.deleteRecursively()
            tempBuildDir.mkdirs()

            gradleGenerator.generate(tempBuildDir)

            copySourceFilesAndGenerateServiceDiscoveryFiles(
                tempBuildDir.toPath(),
            )
            if (!runGradleBuild(tempBuildDir)) {
                return@runCatching false
            }
            moveFinalJar(tempBuildDir.toPath())
            println("✅ Success! Extension JAR created at: ${ContextHolder.OutputConfig.DIR}/${ContextHolder.OutputConfig.JAR_NAME}")
            true
        }.onFailure {
            println("❌ Error building extensions: ${it.message}")
            false
        }.also {
            tempBuildDir.deleteRecursively()
        }.getOrThrow()
    }

    /**
     * Copies source files and generates the Service Loader file for WireMock extensions.
     *
     * @param buildDir The directory where the build will be performed.
     */
    private fun copySourceFilesAndGenerateServiceDiscoveryFiles(buildDir: Path) {
        val fileNames = mutableListOf<String>()
        ContextHolder.SourceFilesConfig.sourceFiles.forEach { file ->

            val fqn = file.substringBeforeLast('.')
            val packageAsPath = fqn.substringBeforeLast('.').replace('.', '/')
            val fileName = file.substringAfterLast(fqn.substringBeforeLast('.') + ".")

            val sourcePath = Path.of(ContextHolder.SourceFilesConfig.sourceFilesLocation, fileName)

            ContextHolder.projectRoot
                .toPath()
                .resolve(sourcePath)
                .takeIf {
                    Files.exists(it)
                }?.let { originalFile ->
                    val destination =
                        buildDir.resolve(
                            "${ResourceUtils.resolveSourceSetPath(originalFile.toFile())}/$packageAsPath/$fileName",
                        )
                    destination.parent.createDirectories()
                    Files.copy(originalFile, destination, StandardCopyOption.REPLACE_EXISTING)
                    fileNames.add(fqn)
                    println("✅ Copied source file: $sourcePath")
                } ?: println("⚠️ Warning: Source file not found and will be skipped: $sourcePath")
        }

        takeIf { fileNames.isNotEmpty() }
            ?.let {
                val servicesDir = buildDir.resolve(ContextHolder.WireMockConfig.META_INF_DIR)
                servicesDir.createDirectories()
                val serviceLoaderFile = servicesDir.resolve(ContextHolder.WireMockConfig.WIREMOCK_SERVICE_LOADER_FILE)
                val serviceLoaderContent = fileNames.joinToString("\n")
                Files.write(serviceLoaderFile, serviceLoaderContent.toByteArray())
                println("✅ Created Service Loader for WireMock to discover extensions.")
            }
            ?: println("⚠️ No extension classes provided, skipping service discovery file generation.")
    }

    /**
     * Runs the Gradle build to compile the extensions and create the JAR file.
     *
     * @param buildDir The directory where the Gradle build will be executed.
     * @return true if the build was successful, false otherwise
     */
    private fun runGradleBuild(buildDir: File): Boolean {
        println("⚙️ Compiling extensions and building JAR...")

        copyGradleWrapper(buildDir)
        val gradleCommand = "gradlew.bat".takeIf { OsUtils.isOsWindows() } ?: "./gradlew"

        return ProcessBuilder(gradleCommand, "shadowJar", "--no-daemon", "-q")
            .directory(buildDir)
            .inheritIO()
            .start()
            .waitFor() == 0
    }

    /**
     * Moves the final JAR file to the specified output directory.
     *
     * @param buildDir The directory where the JAR was built.
     */
    private fun moveFinalJar(buildDir: Path) {
        val sourceJar = buildDir.resolve(ContextHolder.OutputConfig.TEMP_JAR_DIR)
        val targetDir = ContextHolder.projectRoot.toPath().resolve(ContextHolder.OutputConfig.DIR)
        targetDir.createDirectories()
        val targetJar = targetDir.resolve(ContextHolder.OutputConfig.JAR_NAME)
        Files.move(sourceJar, targetJar, StandardCopyOption.REPLACE_EXISTING)
    }

    private fun copyGradleWrapper(buildDir: File) {
        ContextHolder.GradleConfig.WRAPPER_FILES.forEach { path ->
            val targetFile = buildDir.resolve(path)
            ResourceUtils.copyResourceToFile("/$path", targetFile)
            if (path == "gradlew" && !OsUtils.isOsWindows()) {
                targetFile.setExecutable(true)
            }
        }
    }
}
