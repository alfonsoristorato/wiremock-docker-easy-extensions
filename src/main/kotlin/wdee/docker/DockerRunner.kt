package wdee.docker

import wdee.config.ContextHolder
import wdee.utils.Utils.PrintUtils

class DockerRunner(
    private val processBuilder: ProcessBuilder = ProcessBuilder(),
    private val runTime: Runtime = Runtime.getRuntime(),
) {
    /**
     * Runs a WireMock container with the generated extensions using the provided configuration.
     */
    fun runWiremockContainer() {
        PrintUtils.printlnWithIcon(
            icon = PrintUtils.Icon.ROCKET,
            message = "Starting WireMock container '${ContextHolder.JarRunConfig.dockerContainerName}'...",
        )

        val dockerMappingsPath =
            ContextHolder.projectRoot.resolve(ContextHolder.JarRunConfig.wiremockMappingsPath).absolutePath
        val dockerFilesPath =
            ContextHolder.projectRoot.resolve(ContextHolder.JarRunConfig.wiremockFilesPath).absolutePath
        val extensionsJarPath =
            ContextHolder.projectRoot
                .resolve(ContextHolder.OutputConfig.DIR)
                .absolutePath

        val stopCommand =
            listOf(
                "docker",
                "stop",
                ContextHolder.JarRunConfig.dockerContainerName,
            )

        val startCommand =
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

        runTime.addShutdownHook(
            Thread {
                PrintUtils.printlnWithIcon(
                    icon = PrintUtils.Icon.RED_DOT,
                    message = "Shutdown hook triggered. Stopping WireMock container...",
                )
                runCatching {
                    processBuilder
                        .command(
                            stopCommand,
                        ).start()
                        .waitFor()
                }.onFailure {
                    PrintUtils.printlnWithIcon(
                        icon = PrintUtils.Icon.ERROR,
                        message = "Failed to remove WireMock container: ${it.message}",
                    )
                    throw RuntimeException("Failed to remove WireMock container", it)
                }
            },
        )

        runCatching {
            processBuilder
                .command(stopCommand)
                .start()
                .waitFor()

            processBuilder
                .command(startCommand)
                .directory(ContextHolder.projectRoot)
                .inheritIO()
                .start()
                .waitFor()
        }.onFailure {
            PrintUtils.printlnWithIcon(
                icon = PrintUtils.Icon.ERROR,
                message = "Failed to start WireMock container: ${it.message}",
            )
            throw RuntimeException("Failed to start WireMock container", it)
        }
    }
}
