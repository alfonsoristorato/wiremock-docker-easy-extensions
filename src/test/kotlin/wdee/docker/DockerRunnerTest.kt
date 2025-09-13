package wdee.docker

import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FreeSpec
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
import io.mockk.slot
import io.mockk.verifySequence
import utils.TestUtils
import utils.TestUtils.DEFAULT_DOCKER_CONTAINER_NAME
import utils.TestUtils.DEFAULT_DOCKER_PORT
import wdee.config.ContextHolder

// TODO switch back to StringSpec with kotest 6.0.4
class DockerRunnerTest :
    FreeSpec({
        val processBuilder = mockk<ProcessBuilder>()

        val process = mockk<Process>()
        val runtime = mockk<Runtime>()

        val dockerRunner = DockerRunner(processBuilder = processBuilder, runTime = runtime)
        beforeAny {
            every { processBuilder.command(any<List<String>>()) } returns processBuilder
            every { processBuilder.directory(any()) } returns processBuilder
            every { processBuilder.inheritIO() } returns processBuilder
            every { processBuilder.start() } returns process
            every { process.waitFor() } returns 0
            every { runtime.addShutdownHook(any()) } answers { callOriginal() }
        }

        afterAny {
            confirmVerified(
                processBuilder,
                process,
                runtime,
            )
            clearAllMocks()
        }

        withData(
            nameFn = { "runWiremockContainer should run WireMock container with correct parameters and extra command: $it" },
            listOf("--extraCommand"),
            emptyList(),
        ) { wiremockClOptions ->
            TestUtils.mockContextHolder(wiremockClOptions = wiremockClOptions)

            val expectedCommand =
                listOf(
                    "docker",
                    "run",
                    "--rm",
                    "--name",
                    DEFAULT_DOCKER_CONTAINER_NAME,
                    "-p",
                    "$DEFAULT_DOCKER_PORT:8080",
                    "-v",
                    "${ContextHolder.projectRoot.resolve(
                        ContextHolder.JarRunConfig.wiremockMappingsPath,
                    ).absolutePath}:/home/wiremock/mappings",
                    "-v",
                    "${ContextHolder.projectRoot.resolve(
                        ContextHolder.JarRunConfig.wiremockFilesPath,
                    ).absolutePath}:/home/wiremock/__files",
                    "-v",
                    "${ContextHolder.projectRoot.resolve(ContextHolder.OutputConfig.DIR).absolutePath}:/var/wiremock/extensions/",
                    "wiremock/wiremock:3.13.1",
                ) + wiremockClOptions
            val output =
                captureStandardOut {
                    dockerRunner.runWiremockContainer()
                }
            output shouldContain "Starting WireMock container '$DEFAULT_DOCKER_CONTAINER_NAME'..."

            verifySequence {
                // add shutdown hook sequence
                runtime.addShutdownHook(any())

                // start sequence
                processBuilder.command(expectedCommand)
                // This should be the `ContextHolder.projectRoot` instead of `any()`, but because ContextHolder is mocked,
                // mockk throws `No other calls allowed in stdObjectAnswer than equals/hashCode/toString`
                // when doing equality check on `File` object.
                processBuilder.directory(any())
                processBuilder.inheritIO()
                processBuilder.start()
                process.waitFor()
            }
        }

        "runWiremockContainer should handle exceptions thrown during container start gracefully" {
            TestUtils.mockContextHolder()

            every { processBuilder.start() } throws RuntimeException("An Exception during start")

            val output =
                captureStandardOut {
                    shouldThrowAny {
                        dockerRunner.runWiremockContainer()
                    }.apply {
                        message shouldBe "Failed to start WireMock container"
                        cause shouldBe RuntimeException("An Exception during start")
                    }
                }
            output shouldContain "Starting WireMock container '$DEFAULT_DOCKER_CONTAINER_NAME'..."
            output shouldContain "Failed to start WireMock container: An Exception during start"

            verifySequence {
                // add shutdown hook sequence
                runtime.addShutdownHook(any())

                // start sequence
                processBuilder.command(any<List<String>>())
                processBuilder.directory(any())
                processBuilder.inheritIO()
                processBuilder.start()
            }
        }

        "runWiremockContainer should handle shutdown hook correctly" {
            TestUtils.mockContextHolder()

            val hookSlot = slot<Thread>()
            every { runtime.addShutdownHook(capture(hookSlot)) } just runs

            captureStandardOut {
                dockerRunner.runWiremockContainer()
            } shouldContain "Starting WireMock container '$DEFAULT_DOCKER_CONTAINER_NAME'..."

            captureStandardOut {
                hookSlot.captured.run()
            } shouldContain "Shutdown hook triggered. Stopping WireMock container..."

            verifySequence {
                // add shutdown hook sequence
                runtime.addShutdownHook(any())

                // start sequence
                processBuilder.command(any<List<String>>())
                processBuilder.directory(any())
                processBuilder.inheritIO()
                processBuilder.start()
                process.waitFor()

                // shutdown hook sequence
                processBuilder.command(listOf("docker", "stop", DEFAULT_DOCKER_CONTAINER_NAME))
                processBuilder.start()
                process.waitFor()
            }
        }

        "runWiremockContainer shutDown hook should handle exceptions thrown during container stop gracefully" {
            TestUtils.mockContextHolder()

            val hookSlot = slot<Thread>()
            every { runtime.addShutdownHook(capture(hookSlot)) } just runs

            every { processBuilder.start() } returns process andThenThrows RuntimeException("An Exception during stop")

            captureStandardOut {
                dockerRunner.runWiremockContainer()
            } shouldContain "Starting WireMock container '$DEFAULT_DOCKER_CONTAINER_NAME'..."

            captureStandardOut {
                shouldThrowAny {
                    hookSlot.captured.run()
                }.apply {
                    message shouldBe "Failed to remove WireMock container"
                    cause shouldBe RuntimeException("An Exception during stop")
                }
            } shouldContain "Failed to remove WireMock container: An Exception during stop"

            verifySequence {
                // add shutdown hook sequence
                runtime.addShutdownHook(any())

                // start sequence
                processBuilder.command(any<List<String>>())
                processBuilder.directory(any())
                processBuilder.inheritIO()
                processBuilder.start()
                process.waitFor()

                // shutdown hook sequence
                processBuilder.command(listOf("docker", "stop", DEFAULT_DOCKER_CONTAINER_NAME))
                processBuilder.start()
            }
        }
    })
