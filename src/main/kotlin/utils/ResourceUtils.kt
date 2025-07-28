package utils

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object ResourceUtils {
    fun copyResourceToFile(
        resourcePath: String,
        destinationFile: File,
    ) {
        this::class.java.getResourceAsStream(resourcePath)?.use { inputStream ->
            destinationFile.parentFile.mkdirs()
            Files.copy(inputStream, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        } ?: throw IllegalStateException("Resource not found: $resourcePath")
    }
}
