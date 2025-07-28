package builder

import config.Config
import config.OutputConfig
import utils.ResourceUtils
import utils.Utils.isOsWindows
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
     * @param config The configuration that specifies extensions and output.
     * @return true if the build was successful, false otherwise
     */
    fun build(config: Config): Boolean {
        val projectRoot = File(".").canonicalFile
        val tempBuildDir = projectRoot.resolve(".extensions-builder")

        return runCatching {
            tempBuildDir.deleteRecursively()
            tempBuildDir.mkdirs()

            gradleGenerator.generate(tempBuildDir, config)

            copySourceFilesAndGenerateServiceDiscoveryFiles(
                projectRoot.toPath(),
                tempBuildDir.toPath(),
                config.sourceFiles,
                config.sourceFilesLocation,
            )
            if (!runGradleBuild(tempBuildDir)) {
                return@runCatching false
            }
            moveFinalJar(tempBuildDir.toPath(), projectRoot.toPath(), OutputConfig)
            println("✅ Success! Extension JAR created at: ${OutputConfig.DIR}/${OutputConfig.JAR_NAME}")
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
     * @param projectRoot The root directory of the project.
     * @param buildDir The directory where the build will be performed.
     * @param sources List of source files to copy.
     * @param sourceFilesLocation Location of the files. it will be the final package too.
     */
    private fun copySourceFilesAndGenerateServiceDiscoveryFiles(
        projectRoot: Path,
        buildDir: Path,
        sources: List<String>,
        sourceFilesLocation: String,
    ) {
        val fileNames = mutableListOf<String>()
        sources.forEach { file ->
            "$sourceFilesLocation/$file"
                .let { sourcePath ->
                    projectRoot
                        .resolve(sourcePath)
                        .takeIf {
                            Files.exists(it)
                        }?.let {
                            val destination = buildDir.resolve(sourcePath)
                            destination.parent.createDirectories()
                            Files.copy(it, destination, StandardCopyOption.REPLACE_EXISTING)
                            fileNames.add(sourcePath)
                        } ?: println("⚠️ Warning: Source file not found and will be skipped: $sourcePath")
                }
        }

        takeIf { fileNames.isNotEmpty() }
            ?.let {
                val servicesDir = buildDir.resolve("$sourceFilesLocation/resources/META-INF/services")
                servicesDir.createDirectories()
                val serviceLoaderFile = servicesDir.resolve("com.github.tomakehurst.wiremock.extension.Extension")
                val serviceLoaderContent =
                    fileNames.joinToString("\n") { fileName ->
                        fileName.substringBeforeLast('.').replace('/', '.')
                    }
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
        val gradleCommand = takeIf { isOsWindows() }?.let { "gradlew.bat" } ?: "./gradlew"

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
     * @param projectRoot The root directory of the project.
     * @param outputConfig The output configuration specifying the target directory and JAR name.
     */
    private fun moveFinalJar(
        buildDir: Path,
        projectRoot: Path,
        outputConfig: OutputConfig,
    ) {
        val sourceJar = buildDir.resolve("build/libs/extensions-bundled.jar")
        val targetDir = projectRoot.resolve(outputConfig.DIR)
        targetDir.createDirectories()
        val targetJar = targetDir.resolve(outputConfig.JAR_NAME)
        Files.move(sourceJar, targetJar, StandardCopyOption.REPLACE_EXISTING)
    }

    private fun copyGradleWrapper(buildDir: File) {
        val wrapperFiles =
            listOf(
                "gradle/wrapper/gradle-wrapper.jar",
                "gradle/wrapper/gradle-wrapper.properties",
                "gradlew",
                "gradlew.bat",
            )

        wrapperFiles.forEach { path ->
            val targetFile = buildDir.resolve(path)
            ResourceUtils.copyResourceToFile("/$path", targetFile)
            if (path == "gradlew" && !isOsWindows()) {
                targetFile.setExecutable(true)
            }
        }
    }
}
