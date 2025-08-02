package config

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import java.io.File

class ConfigReader {
    /**
     * Reads the configuration from YAML file provided in the specified path and initializes the context.
     * @param configFilePath The path to the configuration file.
     */
    fun readConfigAndInitializeContext(configFilePath: String): Unit =
        File(configFilePath)
            .takeIf {
                it.exists()
            }?.let { configFile ->
                println("📄 Loading configuration from $configFilePath")
                runCatching {
                    ContextHolder.init(
                        config = Yaml().decodeFromStream(Config.serializer(), configFile.inputStream()),
                        configFile = configFile,
                    )
                }.onFailure {
                    println("❌ Error loading configuration: ${it.message}")
                    it.printStackTrace()
                    throw RuntimeException("Error loading configuration: ${it.message}", it)
                }.getOrThrow()
            }
            ?: run {
                println("⚠️ Configuration file not found: $configFilePath")
                throw RuntimeException("Configuration file not found: $configFilePath")
            }
}
