package config

import java.io.File

object ContextHolder {
    lateinit var configDir: String
        private set
    val projectRoot: File = File(".").canonicalFile

    object SourceFilesConfig {
        lateinit var sourceFilesLocation: String
            private set
        lateinit var sourceFiles: List<String>
            private set
        lateinit var dependencies: List<String>
            private set

        internal fun init(config: Config) {
            sourceFilesLocation =
                when (config.sourceFilesLocation) {
                    "", ".", "/", "./" -> configDir
                    else -> "$configDir/${config.sourceFilesLocation}"
                }
            sourceFiles = config.sourceFiles
            dependencies = config.dependencies ?: emptyList()
        }
    }

    object JarRunConfig {
        lateinit var dockerContainerName: String
            private set
        lateinit var dockerPort: String
            private set
        lateinit var wiremockMappingsPath: String
            private set
        lateinit var wiremockFilesPath: String
            private set

        internal fun init(config: Config) {
            dockerContainerName = config.jarRunConfig.dockerContainerName
            dockerPort = config.jarRunConfig.dockerPort.toString()
            wiremockMappingsPath = "$configDir/mappings"
            wiremockFilesPath = "$configDir/__files"
        }
    }

    object OutputConfig {
        const val TEMP_JAR_DIR = "build/libs/extensions-bundled.jar"
        const val DIR: String = "build/extensions"
        const val JAR_NAME: String = "wiremock-extensions-bundled.jar"
    }

    object WireMockConfig {
        const val META_INF_DIR = "src/main/resources/META-INF/services"
        const val WIREMOCK_SERVICE_LOADER_FILE = "com.github.tomakehurst.wiremock.extension.Extension"
    }

    object GradleConfig {
        val WRAPPER_FILES =
            listOf(
                "gradle/wrapper/gradle-wrapper.jar",
                "gradle/wrapper/gradle-wrapper.properties",
                "gradlew",
                "gradlew.bat",
            )
        const val LIBS_FILE_NAME = "libs.versions.toml"
        const val SETTINGS_GRADLE_FILE_NAME = "settings.gradle.kts"
        const val BUILD_GRADLE_FILE_NAME = "build.gradle.kts"
        const val GRADLE_DIR = "gradle"
    }

    fun init(
        config: Config,
        configFile: File,
    ) {
        configDir = configFile.parentFile?.canonicalPath ?: "."
        SourceFilesConfig.init(config)
        JarRunConfig.init(config)
    }
}
