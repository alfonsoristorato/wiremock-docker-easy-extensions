package wdee.utils

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object Utils {
    object OsUtils {
        fun isOsWindows() = System.getProperty("os.name").lowercase().contains("windows")
    }

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

        fun resolveSourceSetPath(file: File) = "src/main/kotlin".takeIf { file.extension == "kt" } ?: "src/main/java"
    }
}
