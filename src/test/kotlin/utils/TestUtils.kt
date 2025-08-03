package utils

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockkObject
import wdee.config.ContextHolder
import java.io.File
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

    fun mockContextHolder(
        configDir: String = ".",
        projectRoot: File = File(".").canonicalFile,
        sourceFilesLocation: String = "src/main/java",
        sourceFiles: List<String> = emptyList(),
        dependencies: List<String> = emptyList(),
        dockerContainerName: String = DEFAULT_DOCKER_CONTAINER_NAME,
        dockerPort: String = DEFAULT_DOCKER_PORT,
        wiremockClOptions: List<String> = DEFAULT_WIREMOCK_CL_OPTIONS,
        wiremockMappingsPath: String = "$configDir/mappings",
        wiremockFilesPath: String = "$configDir/__files",
    ) {
        mockkObject(ContextHolder)
        mockkObject(ContextHolder.SourceFilesConfig)
        mockkObject(ContextHolder.JarRunConfig)
        mockkObject(ContextHolder.OutputConfig)
        mockkObject(ContextHolder.WireMockConfig)
        mockkObject(ContextHolder.GradleConfig)

        every { ContextHolder.init(any(), any()) } just Runs
        every { ContextHolder.configDir } returns configDir
        every { ContextHolder.projectRoot } returns projectRoot

        every { ContextHolder.SourceFilesConfig.init(any()) } just Runs
        every { ContextHolder.SourceFilesConfig.sourceFilesLocation } returns sourceFilesLocation
        every { ContextHolder.SourceFilesConfig.sourceFiles } returns sourceFiles
        every { ContextHolder.SourceFilesConfig.dependencies } returns dependencies

        every { ContextHolder.JarRunConfig.init(any()) } just Runs
        every { ContextHolder.JarRunConfig.dockerContainerName } returns dockerContainerName
        every { ContextHolder.JarRunConfig.dockerPort } returns dockerPort
        every { ContextHolder.JarRunConfig.wiremockClOptions } returns wiremockClOptions
        every { ContextHolder.JarRunConfig.wiremockMappingsPath } returns wiremockMappingsPath
        every { ContextHolder.JarRunConfig.wiremockFilesPath } returns wiremockFilesPath
    }
}
