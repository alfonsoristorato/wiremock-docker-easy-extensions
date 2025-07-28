package docker

import config.JarRunConfig
import config.OutputConfig
import config.WireMockConfigForRunCommand
import java.io.File

class DockerRunner {
    /**
     * Runs a WireMock container with the generated extensions.
     */
    fun runWiremockContainer(
        jarRunConfig: JarRunConfig,
        wireMockConfigForRunCommand: WireMockConfigForRunCommand,
        outputConfig: OutputConfig,
        projectRoot: File,
    ) {
        println("\n🚀 Starting WireMock container '$jarRunConfig.dockerContainerName'...")

        val dockerMappingsPath = projectRoot.resolve(wireMockConfigForRunCommand.mappingsDir).absolutePath
        val dockerFilesPath = projectRoot.resolve(wireMockConfigForRunCommand.filesDir).absolutePath
        val extensionsJarPath =
            projectRoot
                .resolve(outputConfig.DIR)
                .absolutePath

        val command =
            listOf(
                "docker",
                "run",
                "--rm",
                "--name",
                jarRunConfig.dockerContainerName,
                "-p",
                "${jarRunConfig.dockerPort}:8080",
                "-v",
                "$dockerMappingsPath:/home/wiremock/mappings",
                "-v",
                "$dockerFilesPath:/home/wiremock/__files",
                "-v",
                "$extensionsJarPath:/var/wiremock/extensions/",
                "wiremock/wiremock:3.13.1",
                "--verbose",
            )

        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("\n🛑 Stopping WireMock container...")
                try {
                    ProcessBuilder("docker", "stop", jarRunConfig.dockerContainerName).start().waitFor()
                } catch (e: Exception) {
                    System.err.println("Failed to stop container: ${e.message}")
                }
            },
        )

        try {
            val process =
                ProcessBuilder(command)
                    .directory(projectRoot)
                    .inheritIO()
                    .start()

            process.waitFor()
        } catch (e: Exception) {
            System.err.println("Failed to start WireMock container: ${e.message}")
        }
    }
}
