package wdee.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Config(
    @SerialName("source-files-location") val sourceFilesLocation: String,
    @SerialName("source-files") val sourceFiles: List<String>,
    val dependencies: List<String>?,
    @SerialName("jar-run-config") val jarRunConfig: JarRunConfig = JarRunConfig(),
)

@Serializable
data class JarRunConfig(
    @SerialName("docker-container-name") val dockerContainerName: String = "wiremock-docker-easy-extensions",
    @SerialName("docker-port")val dockerPort: Int = 8080,
)
