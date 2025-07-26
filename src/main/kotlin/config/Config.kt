package config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Config(
    @SerialName("files-location") val filesLocation: String,
    @SerialName("source-files") val sourceFiles: List<String>,
    val dependencies: List<String>?,
    val output: OutputConfig,
    @SerialName("wiremock") val wiremockConfig: WireMockConfig,
)

@Serializable
data class OutputConfig(
    val dir: String,
    @SerialName("jar-name") val jarName: String,
)

@Serializable
data class WireMockConfig(
    @SerialName("mappings-dir")val mappingsDir: String,
    // TODO make this optional
    @SerialName("files-dir")val filesDir: String,
)
