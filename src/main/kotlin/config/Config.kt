package config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Config(
    @SerialName("extension-classes") val extensionClasses: List<String>,
    @SerialName("source-files")  val sourceFiles: List<String>,
    val dependencies: List<String>?,
    @SerialName("use-gradle-wrapper") val useGradleWrapper: Boolean,
    val output: OutputConfig,
    val docker: DockerConfig
)

@Serializable

data class OutputConfig(
    val dir: String,
    @SerialName("jar-name") val jarName: String
)