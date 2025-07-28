package config

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import utils.TestUtils.DEFAULT_DOCKER_CONTAINER_NAME
import utils.TestUtils.DEFAULT_DOCKER_PORT
import utils.TestUtils.getConfigFileFromResources

class ConfigLoaderTest :
    StringSpec({

        "should load and parse the config file correctly" {
            val configFile = getConfigFileFromResources("test-config.yaml")
            val configLoader = ConfigLoader()

            val config = configLoader.loadConfig(configFile.absolutePath)

            config.sourceFilesLocation shouldBe "src/main/kotlin"
            config.sourceFiles shouldBe listOf("File1.kt", "File2.java")
            config.dependencies shouldBe listOf("org.apache.commons:commons-lang3:3.18.0")
        }

        "should throw exception when config file does not exist" {
            val configLoader = ConfigLoader()

            val exception =
                shouldThrow<RuntimeException> {
                    configLoader.loadConfig("non-existent-file.yaml")
                }

            exception.message shouldBe "Configuration file not found: non-existent-file.yaml"
        }

        "should handle config with empty dependencies list" {
            val configFile = getConfigFileFromResources("test-config-empty-list-deps.yaml")
            val configLoader = ConfigLoader()

            val config = configLoader.loadConfig(configFile.absolutePath)

            config.dependencies shouldBe emptyList()
        }

        "should handle config with null dependencies list" {
            val configFile = getConfigFileFromResources("test-config-null-deps.yaml")
            val configLoader = ConfigLoader()

            val config = configLoader.loadConfig(configFile.absolutePath)

            config.dependencies shouldBe null
        }

        "should handle config with single source file" {
            val configFile = getConfigFileFromResources("test-config-single-file.yaml")
            val configLoader = ConfigLoader()

            val config = configLoader.loadConfig(configFile.absolutePath)

            config.sourceFilesLocation shouldBe "src/main/kotlin"
            config.sourceFiles shouldBe listOf("File1.kt")
            config.dependencies shouldBe listOf("org.apache.commons:commons-lang3:3.18.0")
        }

        "should use jarRunConfig if provided" {
            val configFile = getConfigFileFromResources("test-config-with-jar-run-config.yaml")
            val configLoader = ConfigLoader()

            val config = configLoader.loadConfig(configFile.absolutePath)

            config.jarRunConfig.dockerContainerName shouldBe "my-test-container"
            config.jarRunConfig.dockerPort shouldBe 9090
        }

        "should default jarRunConfig.dockerPort if not provided" {
            val configFile = getConfigFileFromResources("test-config-with-jar-run-config-no-port.yaml")
            val configLoader = ConfigLoader()

            val config = configLoader.loadConfig(configFile.absolutePath)

            config.jarRunConfig.dockerContainerName shouldBe "my-test-container"
            config.jarRunConfig.dockerPort shouldBe DEFAULT_DOCKER_PORT
        }

        "should default jarRunConfig.dockerContainerName if not provided" {
            val configFile = getConfigFileFromResources("test-config-with-jar-run-config-no-container-name.yaml")
            val configLoader = ConfigLoader()

            val config = configLoader.loadConfig(configFile.absolutePath)

            config.jarRunConfig.dockerContainerName shouldBe DEFAULT_DOCKER_CONTAINER_NAME
            config.jarRunConfig.dockerPort shouldBe 9090
        }

        "should default whole JarRunConfig if not provided" {
            val configFile = getConfigFileFromResources("test-config.yaml")
            val configLoader = ConfigLoader()

            val config = configLoader.loadConfig(configFile.absolutePath)

            config.jarRunConfig.dockerContainerName shouldBe DEFAULT_DOCKER_CONTAINER_NAME
            config.jarRunConfig.dockerPort shouldBe DEFAULT_DOCKER_PORT
        }
    })
