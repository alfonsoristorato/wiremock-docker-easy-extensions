package config

import com.charleskorn.kaml.Yaml
import java.io.File

class ConfigLoader {
    /**
     * Loads the configuration from YAML file provided in the specified path.
     */
    fun loadConfig(configFilePath: String): Config =
        File(configFilePath)
            .takeIf {
                it.exists()
            }?.let { configFile ->
                println("📄 Loading configuration from $configFilePath")
                runCatching {
                    Yaml().decodeFromStream(Config.serializer(), configFile.inputStream())
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
