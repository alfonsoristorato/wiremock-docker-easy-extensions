package wdee.docker

import wdee.config.ContextHolder

class DockerRunner {
    /**
     * Runs a WireMock container with the generated extensions using the provided configuration.
     */
    fun runWiremockContainer() {
        println("\n🚀 Starting WireMock container '${ContextHolder.JarRunConfig.dockerContainerName}'...")

        val dockerMappingsPath =
            ContextHolder.projectRoot.resolve(ContextHolder.JarRunConfig.wiremockMappingsPath).absolutePath
        val dockerFilesPath =
            ContextHolder.projectRoot.resolve(ContextHolder.JarRunConfig.wiremockFilesPath).absolutePath
        val extensionsJarPath =
            ContextHolder.projectRoot
                .resolve(ContextHolder.OutputConfig.DIR)
                .absolutePath

        val command =
            listOf(
                "docker",
                "run",
                "--rm",
                "--name",
                ContextHolder.JarRunConfig.dockerContainerName,
                "-p",
                "${ContextHolder.JarRunConfig.dockerPort}:8080",
                "-v",
                "$dockerMappingsPath:/home/wiremock/mappings",
                "-v",
                "$dockerFilesPath:/home/wiremock/__files",
                "-v",
                "$extensionsJarPath:/var/wiremock/extensions/",
                "wiremock/wiremock:3.13.1",
            ) + ContextHolder.JarRunConfig.wiremockClOptions

        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("\n🛑 Stopping WireMock container...")
                try {
                    ProcessBuilder("docker", "stop", ContextHolder.JarRunConfig.dockerContainerName).start().waitFor()
                } catch (e: Exception) {
                    System.err.println("Failed to stop container: ${e.message}")
                }
            },
        )

        try {
            val process =
                ProcessBuilder(command)
                    .directory(ContextHolder.projectRoot)
                    .inheritIO()
                    .start()

            process.waitFor()
        } catch (e: Exception) {
            System.err.println("Failed to start WireMock container: ${e.message}")
        }
    }
}
