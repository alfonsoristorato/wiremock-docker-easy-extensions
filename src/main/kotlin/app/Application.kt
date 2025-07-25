package app

import builder.ExtensionBuilder
import config.ConfigLoader
import docker.DockerRunner
import java.io.File
import kotlin.system.exitProcess

class Application {
    private val configLoader = ConfigLoader()
    private val extensionBuilder = ExtensionBuilder()
    private val dockerRunner = DockerRunner()

    /**
     * Run the application with the provided command line arguments.
     */
    fun run(args: Array<String>) {
        if (args.size != 2 && !(args.size == 1 && args[0] == "help")) {
            println("Error: Invalid arguments. Please provide a command and a config file.")
            printUsage()
            exitProcess(1)
        }
        val command = args[0]

        if (command == "help") {
            printUsage()
            return
        }
        val configFile = args[1]
        val config = configLoader.loadConfig(configFile)

        when (command) {
            "build" -> {
                val success = extensionBuilder.build(config)
                if (!success) {
                    exitProcess(1)
                }
            }
            "run" -> {
                val success = extensionBuilder.build(config)
                if (success) {
                    val projectRoot = File(".").canonicalFile
                    dockerRunner.runWiremockContainer(
                        config.wiremockConfig,
                        config.output,
                        projectRoot,
                    )
                } else {
                    exitProcess(1)
                }
            }
            else -> {
                println("Unknown command: $command")
                printUsage()
                exitProcess(1)
            }
        }
    }

    private fun printUsage() {
        println(
            """
            
            WireMock Extension Builder

            Usage:
              <command> <config-file>

            Commands:
              build <config-file>  - Build extensions JAR.
              run <config-file>    - Build extensions JAR and run WireMock Docker container.
              help                 - Show this help message.
            """.trimIndent(),
        )
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Application().run(args)
        }
    }
}
