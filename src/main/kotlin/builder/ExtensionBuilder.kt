package builder

import config.Config
import config.OutputConfig
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.createDirectories


class ExtensionBuilder(
    private val gradleGenerator: GradleProjectGenerator = GradleProjectGenerator()
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
                config.filesLocation
            )
            if (!runGradleBuild(tempBuildDir, config.useGradleWrapper)) {
                return@runCatching false
            }
            moveFinalJar(tempBuildDir.toPath(), projectRoot.toPath(), config.output)
            println("✅ Success! Extension JAR created at: ${config.output.dir}/${config.output.jarName}")
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
     * @param filesLocation Location of the files. it will be the final package too.
     */
    private fun copySourceFilesAndGenerateServiceDiscoveryFiles(
        projectRoot: Path,
        buildDir: Path,
        sources: List<String>,
        filesLocation: String
    ) {
        val fileNames = mutableListOf<String>()
        sources.forEach { file ->
            "$filesLocation/$file"
                .let { sourcePath ->
                    projectRoot.resolve(sourcePath)
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
                val servicesDir = buildDir.resolve("$filesLocation/resources/META-INF/services")
                servicesDir.createDirectories()
                val serviceLoaderFile = servicesDir.resolve("com.github.tomakehurst.wiremock.extension.Extension")
                val serviceLoaderContent = fileNames.joinToString("\n") { fileName ->
                    fileName.substringBeforeLast('.').replace('/', '.')
                }
                Files.write(serviceLoaderFile, serviceLoaderContent.toByteArray())
                println("✅ Created Service Loader for wiremock to discover extensions.")
            }
            ?: println("⚠️ No extension classes provided, skipping service discovery file generation.")
    }

    /**
     * Runs the Gradle build to compile the extensions and create the JAR file.
     *
     * @param buildDir The directory where the Gradle build will be executed.
     * @param useGradleWrapper Whether to use the Gradle wrapper or the system Gradle installation.
     * @return true if the build was successful, false otherwise
     */
    private fun runGradleBuild(buildDir: File, useGradleWrapper: Boolean): Boolean {
        println("⚙️ Compiling extensions and building JAR...")

        val gradleCommand = when (useGradleWrapper) {
            true -> {
                copyGradleWrapper(File(".").canonicalFile, buildDir)
                //TODO test this on Windows and Linux :)
                if (System.getProperty("os.name").lowercase().contains("windows")) {
                    "gradlew.bat"
                } else {
                    "./gradlew"
                }
            }
            false -> "gradle"
        }


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
    private fun moveFinalJar(buildDir: Path, projectRoot: Path, outputConfig: OutputConfig) {
        val sourceJar = buildDir.resolve("build/libs/extensions-bundled.jar")
        val targetDir = projectRoot.resolve(outputConfig.dir)
        targetDir.createDirectories()
        val targetJar = targetDir.resolve(outputConfig.jarName)
        Files.move(sourceJar, targetJar, StandardCopyOption.REPLACE_EXISTING)
    }

    private fun copyGradleWrapper(projectRoot: File, buildDir: File) {
        val gradleWrapperDir = buildDir.resolve("gradle/wrapper")
        gradleWrapperDir.mkdirs()

        val wrapperFiles = listOf(
            "gradle/wrapper/gradle-wrapper.jar",
            "gradle/wrapper/gradle-wrapper.properties",
            "gradlew",
            "gradlew.bat"
        )

        wrapperFiles.forEach { path ->
            val sourceFile = projectRoot.resolve(path)
            if (sourceFile.exists()) {
                val targetFile = buildDir.resolve(path)
                targetFile.parentFile.mkdirs()
                sourceFile.copyTo(targetFile, overwrite = true)

                //TODO See if this works on Linux and a Mac that doesn't restrict usage of gradleW
                if (path == "gradlew" && !System.getProperty("os.name").lowercase().contains("windows")) {
                    targetFile.setExecutable(true)
                }
            } else {
                println("⚠️ Warning: Gradle wrapper file not found: $path")
            }
        }
    }
}