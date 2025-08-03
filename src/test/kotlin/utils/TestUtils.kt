package utils

import java.nio.file.Paths

object TestUtils {
    class TestExitException(
        val status: Int,
    ) : RuntimeException("Test exit with status $status")

    const val DEFAULT_DOCKER_PORT: String = "8080"
    const val DEFAULT_DOCKER_CONTAINER_NAME: String = "wiremock-docker-easy-extensions"
    val DEFAULT_WIREMOCK_CL_OPTIONS: List<String> = emptyList()

    fun getConfigFileFromResources(resource: String) =
        javaClass.classLoader
            .getResource(resource)
            ?.let { url ->
                Paths.get(url.toURI()).toFile()
            }
            ?: throw RuntimeException("Configuration file not found: $resource")
}
