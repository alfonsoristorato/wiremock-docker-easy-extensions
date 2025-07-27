package config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Config(
    @SerialName("files-location") val filesLocation: String,
    @SerialName("source-files") val sourceFiles: List<String>,
    val dependencies: List<String>?,
)

@ConsistentCopyVisibility
data class WireMockConfigForRunCommand private constructor(
    private val filesLocation: String,
    val mappingsDir: String,
    val filesDir: String,
) {
    constructor(filesLocation: String) : this(
        filesLocation,
        "${filesLocation.substringBefore("/")}/mappings",
        "${filesLocation.substringBefore("/")}/__files",
    )
}

object OutputConfig {
    const val DIR: String = "build/extensions"
    const val JAR_NAME: String = "wiremock-extensions-bundled.jar"
}
