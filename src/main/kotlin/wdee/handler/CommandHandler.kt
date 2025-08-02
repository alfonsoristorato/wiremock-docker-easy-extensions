package wdee.handler

import wdee.builder.ExtensionBuilder
import wdee.config.ConfigReader
import wdee.docker.DockerRunner
import kotlin.system.exitProcess

class CommandHandler {
    private val configReader = ConfigReader()
    private val extensionBuilder = ExtensionBuilder()
    private val dockerRunner = DockerRunner()

    /**
     * Run the application with the provided command line arguments.
     * @param args Command line arguments
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
        configReader.readConfigAndInitializeContext(configFile)

        when (command) {
            "build" -> {
                val success = extensionBuilder.build()
                if (!success) {
                    exitProcess(1)
                }
            }
            "run" -> {
                val success = extensionBuilder.build()
                if (success) {
                    dockerRunner.runWiremockContainer()
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
}
