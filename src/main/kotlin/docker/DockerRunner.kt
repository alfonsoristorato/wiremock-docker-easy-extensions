package docker

import config.WireMockConfig
import config.OutputConfig
import java.io.File

// TODO most likely this will go away and the app will run on build and Docker Image only
class DockerRunner {
    /**
     * Runs a WireMock container with the generated extensions.
     */
    fun runWiremockContainer(
        docker: WireMockConfig,
        output: OutputConfig,
        projectRoot: File,
    ) {
        val containerName = "wiremock-docker-easy-extensions"
        println("\n🚀 Starting WireMock container '$containerName'...")

        val dockerMappingsPath = projectRoot.resolve(docker.mappingsDir).absolutePath
        val dockerFilesPath = projectRoot.resolve(docker.filesDir).absolutePath
        val extensionsJarPath =
            projectRoot
                .resolve(output.dir)
                .absolutePath

        val command =
            listOf(
                "docker",
                "run",
                "--rm",
                "--name",
                containerName,
                "-p",
                "8080:8080",
                "-v",
                "$dockerMappingsPath:/home/wiremock/mappings",
                "-v",
                "$dockerFilesPath:/home/wiremock/__files",
                "-v",
                "$extensionsJarPath:/var/wiremock/extensions/",
                "wiremock/wiremock:latest",
                "--verbose",
            )

        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("\n🛑 Stopping WireMock container...")
                try {
                    ProcessBuilder("docker", "stop", containerName).start().waitFor()
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
