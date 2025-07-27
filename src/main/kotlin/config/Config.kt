package config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Config(
    @SerialName("source-files-location") val sourceFilesLocation: String,
    @SerialName("source-files") val sourceFiles: List<String>,
    val dependencies: List<String>?,
)

@ConsistentCopyVisibility
data class WireMockConfigForRunCommand private constructor(
    private val sourceFilesLocation: String,
    val mappingsDir: String,
    val filesDir: String,
) {
    constructor(sourceFilesLocation: String) : this(
        sourceFilesLocation,
        "${sourceFilesLocation.substringBefore("/")}/mappings",
        "${sourceFilesLocation.substringBefore("/")}/__files",
    )
}

object OutputConfig {
    const val DIR: String = "build/extensions"
    const val JAR_NAME: String = "wiremock-extensions-bundled.jar"
}
