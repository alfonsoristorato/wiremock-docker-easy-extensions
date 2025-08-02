package wdee.config

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import utils.TestUtils.DEFAULT_DOCKER_CONTAINER_NAME
import utils.TestUtils.DEFAULT_DOCKER_PORT
import utils.TestUtils.DEFAULT_WIREMOCK_CL_OPTIONS
import utils.TestUtils.getConfigFileFromResources
import java.io.File

class ConfigReaderTest :
    StringSpec({

        "should load and parse the config file correctly - setting ContextHolder" {
            val configFile = getConfigFileFromResources("test-config/wdee-config.yaml")
            val configReader = ConfigReader()

            configReader.readConfigAndInitializeContext(configFile.absolutePath)
            ContextHolder.configDir shouldBe configFile.parentFile.absolutePath
            ContextHolder.projectRoot shouldBe File(".").canonicalFile
            ContextHolder.SourceFilesConfig.sourceFilesLocation shouldEndWith "src/main/kotlin"
            ContextHolder.SourceFilesConfig.sourceFiles shouldBe listOf("File1.kt", "File2.java")
            ContextHolder.SourceFilesConfig.dependencies shouldBe listOf("org.apache.commons:commons-lang3:3.18.0")
            ContextHolder.JarRunConfig.dockerContainerName shouldBe DEFAULT_DOCKER_CONTAINER_NAME
            ContextHolder.JarRunConfig.dockerPort shouldBe DEFAULT_DOCKER_PORT
            ContextHolder.JarRunConfig.wiremockClOptions shouldBe DEFAULT_WIREMOCK_CL_OPTIONS
            ContextHolder.JarRunConfig.wiremockMappingsPath shouldEndWith "mappings"
            ContextHolder.JarRunConfig.wiremockFilesPath shouldEndWith "__files"
        }

        "should throw exception when config file does not exist" {
            val configReader = ConfigReader()

            val exception =
                shouldThrow<RuntimeException> {
                    configReader.readConfigAndInitializeContext("non-existent-file.yaml")
                }

            exception.message shouldBe "Configuration file not found: non-existent-file.yaml"
        }

        "should throw exception when config file is not named correctly" {
            val configReader = ConfigReader()
            val configFile = getConfigFileFromResources("test-config/wrong-name-config.yaml")

            val exception =
                shouldThrow<RuntimeException> {
                    configReader.readConfigAndInitializeContext(configFile.absolutePath)
                }

            exception.message shouldBe
                "Invalid configuration file name. Expected '${ContextHolder.CONFIG_FILE_NAME}', got 'wrong-name-config.yaml'"
        }

        "should handle config with empty dependencies list" {
            val configFile = getConfigFileFromResources("test-config-empty-list-deps/wdee-config.yaml")
            val configReader = ConfigReader()

            configReader.readConfigAndInitializeContext(configFile.absolutePath)

            ContextHolder.SourceFilesConfig.dependencies shouldBe emptyList()
        }

        "should handle config with null dependencies list" {
            val configFile = getConfigFileFromResources("test-config-null-deps/wdee-config.yaml")
            val configReader = ConfigReader()

            configReader.readConfigAndInitializeContext(configFile.absolutePath)

            ContextHolder.SourceFilesConfig.dependencies shouldBe emptyList()
        }

        "should handle config with single source file" {
            val configFile = getConfigFileFromResources("test-config-single-file/wdee-config.yaml")
            val configReader = ConfigReader()

            configReader.readConfigAndInitializeContext(configFile.absolutePath)

            ContextHolder.SourceFilesConfig.sourceFilesLocation shouldEndWith "src/main/kotlin"
            ContextHolder.SourceFilesConfig.sourceFiles shouldBe listOf("File1.kt")
            ContextHolder.SourceFilesConfig.dependencies shouldBe listOf("org.apache.commons:commons-lang3:3.18.0")
        }

        "should use jarRunConfig if provided" {
            val configFile = getConfigFileFromResources("test-config-with-jar-run-config/wdee-config.yaml")
            val configReader = ConfigReader()

            configReader.readConfigAndInitializeContext(configFile.absolutePath)

            ContextHolder.JarRunConfig.dockerContainerName shouldBe "my-test-container"
            ContextHolder.JarRunConfig.dockerPort shouldBe "9090"
            ContextHolder.JarRunConfig.wiremockClOptions shouldBe listOf("--verbose", "--record-mappings")
        }

        "should default jarRunConfig.dockerPort if not provided" {
            val configFile = getConfigFileFromResources("test-config-with-jar-run-config-no-port/wdee-config.yaml")
            val configReader = ConfigReader()

            configReader.readConfigAndInitializeContext(configFile.absolutePath)

            ContextHolder.JarRunConfig.dockerContainerName shouldBe "my-test-container"
            ContextHolder.JarRunConfig.dockerPort shouldBe DEFAULT_DOCKER_PORT
            ContextHolder.JarRunConfig.wiremockClOptions shouldBe listOf("--verbose", "--record-mappings")
        }

        "should default jarRunConfig.dockerContainerName if not provided" {
            val configFile = getConfigFileFromResources("test-config-with-jar-run-config-no-container-name/wdee-config.yaml")
            val configReader = ConfigReader()

            configReader.readConfigAndInitializeContext(configFile.absolutePath)

            ContextHolder.JarRunConfig.dockerContainerName shouldBe DEFAULT_DOCKER_CONTAINER_NAME
            ContextHolder.JarRunConfig.dockerPort shouldBe "9090"
            ContextHolder.JarRunConfig.wiremockClOptions shouldBe listOf("--verbose", "--record-mappings")
        }

        "should default jarRunConfig.wiremockClOptions if null in config" {
            val configFile = getConfigFileFromResources("test-config-with-jar-run-config-null-wiremock-cl-options/wdee-config.yaml")
            val configReader = ConfigReader()

            configReader.readConfigAndInitializeContext(configFile.absolutePath)

            ContextHolder.JarRunConfig.wiremockClOptions shouldBe emptyList()
        }

        "should default jarRunConfig.wiremockClOptions if empty in config" {
            val configFile = getConfigFileFromResources("test-config-with-jar-run-config-empty-wiremock-cl-options/wdee-config.yaml")
            val configReader = ConfigReader()

            configReader.readConfigAndInitializeContext(configFile.absolutePath)

            ContextHolder.JarRunConfig.wiremockClOptions shouldBe emptyList()
        }

        "should default whole JarRunConfig if not provided" {
            val configFile = getConfigFileFromResources("test-config/wdee-config.yaml")
            val configReader = ConfigReader()

            configReader.readConfigAndInitializeContext(configFile.absolutePath)

            ContextHolder.JarRunConfig.dockerContainerName shouldBe DEFAULT_DOCKER_CONTAINER_NAME
            ContextHolder.JarRunConfig.dockerPort shouldBe DEFAULT_DOCKER_PORT
            ContextHolder.JarRunConfig.wiremockClOptions shouldBe DEFAULT_WIREMOCK_CL_OPTIONS
        }
    })
