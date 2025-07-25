package config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DockerConfig(
    @SerialName("mappings-dir")val mappingsDir: String,
    @SerialName("files-dir")val filesDir: String
)