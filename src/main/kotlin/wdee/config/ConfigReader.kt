package wdee.config

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import wdee.utils.Utils.PrintUtils
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
                if (configFile.name != ContextHolder.CONFIG_FILE_NAME) {
                    "Invalid configuration file name. Expected '${ContextHolder.CONFIG_FILE_NAME}', got '${configFile.name}'"
                        .let {
                            PrintUtils.printlnWithIcon(
                                icon = PrintUtils.Icon.ERROR,
                                message = it,
                            )
                            throw RuntimeException(it)
                        }
                }
                PrintUtils.printlnWithIcon(
                    icon = PrintUtils.Icon.PAGE,
                    message = "Loading configuration from $configFilePath",
                )
                runCatching {
                    ContextHolder.init(
                        config = Yaml().decodeFromStream(Config.serializer(), configFile.inputStream()),
                        configFile = configFile,
                    )
                }.onFailure {
                    PrintUtils.printlnWithIcon(
                        icon = PrintUtils.Icon.ERROR,
                        message = "Error loading configuration: ${it.message}",
                    )
                    it.printStackTrace()
                    throw RuntimeException("Error loading configuration: ${it.message}", it)
                }.getOrThrow()
            }
            ?: run {
                PrintUtils.printlnWithIcon(
                    icon = PrintUtils.Icon.WARNING,
                    message = "Configuration file not found: $configFilePath",
                )
                throw RuntimeException("Configuration file not found: $configFilePath")
            }
}
