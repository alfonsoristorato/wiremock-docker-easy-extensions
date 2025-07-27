package utils

import java.nio.file.Paths

object TestUtils {
    fun getConfigFileFromResources(fileName: String) =
        javaClass.classLoader
            .getResource(fileName)
            ?.let { resource ->
                Paths.get(resource.toURI()).toFile()
            }
            ?: throw RuntimeException("Configuration file not found: $fileName")
}
