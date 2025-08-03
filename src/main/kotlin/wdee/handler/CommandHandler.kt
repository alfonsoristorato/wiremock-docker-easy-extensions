package wdee.handler

import wdee.builder.ExtensionBuilder
import wdee.config.ConfigReader
import wdee.docker.DockerRunner
import kotlin.system.exitProcess

class CommandHandler(
    private val configReader: ConfigReader = ConfigReader(),
    private val extensionBuilder: ExtensionBuilder = ExtensionBuilder(),
    private val dockerRunner: DockerRunner = DockerRunner(),
    private val exitFunction: (Int) -> Unit = { exitProcess(it) },
) {
    /**
     * Run the application with the provided command line arguments.
     * @param args Command line arguments
     */
    fun run(args: Array<String>) {
        if (args.size != 2 && !(args.size == 1 && args[0] == "help")) {
            println("Error: Invalid arguments. Please provide a command and a config file.")
            printUsage()
            exitFunction(1)
        }
        val command = args[0]

        if (command == "help") {
            printUsage()
            return
        }
        val configFile = args[1]

        when (command) {
            "build" -> {
                configReader.readConfigAndInitializeContext(configFile)
                val success = extensionBuilder.build()
                if (!success) {
                    exitFunction(1)
                }
            }
            "run" -> {
                configReader.readConfigAndInitializeContext(configFile)
                val success = extensionBuilder.build()
                if (success) {
                    dockerRunner.runWiremockContainer()
                } else {
                    exitFunction(1)
                }
            }
            else -> {
                println("Unknown command: $command")
                printUsage()
                exitFunction(1)
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
