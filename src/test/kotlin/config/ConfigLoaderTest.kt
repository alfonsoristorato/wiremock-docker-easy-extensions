package config

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
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
    })
