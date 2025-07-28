package utils

import java.nio.file.Paths

object TestUtils {
    const val DEFAULT_DOCKER_PORT: Int = 8080
    const val DEFAULT_DOCKER_CONTAINER_NAME: String = "wiremock-docker-easy-extensions"

    fun getConfigFileFromResources(fileName: String) =
        javaClass.classLoader
            .getResource(fileName)
            ?.let { resource ->
                Paths.get(resource.toURI()).toFile()
            }
            ?: throw RuntimeException("Configuration file not found: $fileName")
}
