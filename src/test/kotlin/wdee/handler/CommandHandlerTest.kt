package wdee.handler

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.data.row
import io.kotest.datatest.withData
import io.kotest.extensions.system.captureStandardOut
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verifySequence
import utils.TestUtils.TestExitException
import wdee.builder.ExtensionBuilder
import wdee.config.ConfigReader
import wdee.docker.DockerRunner

// TODO switch back to StringSpec with kotest 6.0.4
class CommandHandlerTest :
    FreeSpec({
        val configReader = mockk<ConfigReader>()
        val extensionBuilder = mockk<ExtensionBuilder>()
        val dockerRunner = mockk<DockerRunner>()
        val exitFunction: (Int) -> Unit = mockk()

        val commandHandler =
            CommandHandler(
                configReader = configReader,
                extensionBuilder = extensionBuilder,
                dockerRunner = dockerRunner,
                exitFunction = exitFunction,
            )

        beforeAny {
            every { exitFunction(1) } answers { throw TestExitException(firstArg()) }
            every { extensionBuilder.build() } returns true
            every { configReader.readConfigAndInitializeContext(any()) } just runs
            every { dockerRunner.runWiremockContainer() } just runs
        }
        afterAny {
            confirmVerified(
                configReader,
                extensionBuilder,
                dockerRunner,
                exitFunction,
            )
            clearAllMocks()
        }

        """should call extensionBuilder.build when `build` command is executed 
        and proceed if extensionBuilder.build is successful""" {
            commandHandler.run(arrayOf("build", "config.json"))

            verifySequence {
                configReader.readConfigAndInitializeContext("config.json")
                extensionBuilder.build()
            }
        }

        """should call extensionBuilder.build when `build` command is executed 
        and stop if extensionBuilder.build is not successful""" {
            every { extensionBuilder.build() } returns false

            shouldThrow<TestExitException> {
                commandHandler.run(arrayOf("build", "config.json"))
            }.status shouldBe 1

            verifySequence {
                configReader.readConfigAndInitializeContext("config.json")
                extensionBuilder.build()
                exitFunction(1)
            }
        }

        """should call extensionBuilder.build and dockerRunner.runWiremockContainer when`run` command is executed 
        and proceed if extensionBuilder.build is successful""" {
            commandHandler.run(arrayOf("run", "config.json"))

            verifySequence {
                configReader.readConfigAndInitializeContext("config.json")
                extensionBuilder.build()
                dockerRunner.runWiremockContainer()
            }
        }

        """should call extensionBuilder.build and not dockerRunner.runWiremockContainer when`run` command is executed 
        and stop if extensionBuilder.build is not successful""" {
            every { extensionBuilder.build() } returns false

            shouldThrow<TestExitException> {
                commandHandler.run(arrayOf("run", "config.json"))
            }.status shouldBe 1

            verifySequence {
                configReader.readConfigAndInitializeContext("config.json")
                extensionBuilder.build()
                exitFunction(1)
            }
        }

        withData(
            nameFn = { "should handle wrong commands gracefully: [${it.a.joinToString(", ")}]" },
            row(arrayOf("aCommand"), "Error: Invalid arguments. Please provide a command and a config file."),
            row(arrayOf("aCommand", "config.json"), "Unknown command: aCommand"),
            row(emptyArray(), "Error: Invalid arguments. Please provide a command and a config file."),
        ) { (args, expectedMessage) ->
            val output =
                captureStandardOut {
                    val exception =
                        shouldThrow<TestExitException> {
                            commandHandler.run(args)
                        }
                    exception.status shouldBe 1
                }

            verifySequence {
                exitFunction(1)
            }

            output shouldContain expectedMessage
            output shouldContain "Usage:"
            output shouldContain "build <config-file>"
            output shouldContain "run <config-file>"
        }

        "should print usage instructions when requested" {
            val output =
                captureStandardOut {
                    commandHandler.run(arrayOf("help"))
                }

            output shouldContain "Usage:"
            output shouldContain "build <config-file>"
            output shouldContain "run <config-file>"
        }
    })
