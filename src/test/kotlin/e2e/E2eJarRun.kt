package e2e

import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import utils.HttpUtils
import utils.TestUtils
import java.io.File
import kotlin.time.Duration.Companion.seconds

class E2eJarRun :
    FunSpec({
        val projectDir = File(".").canonicalFile
        val e2eFilesDir = TestUtils.getConfigFileFromResources("e2e-resources")
        val configFile = File(e2eFilesDir, "wdee-config.yaml")
        val wdeeBuiltJar = File(projectDir, "build/libs/wiremock-docker-easy-extensions.jar")
        val containerName = "wiremock-docker-easy-extensions"
        val port = 8080
        val processBuilder = ProcessBuilder()
        val eventuallyWait = 30.seconds
        lateinit var wdeeProcess: Process

        fun dockerAvailable(): Boolean =
            runCatching {
                processBuilder.command(listOf("docker", "ps")).start().waitFor() == 0
            }.getOrDefault(false)

        fun wdeeRunning() = HttpUtils.get("http://localhost:$port/__admin/health")

        fun stopContainer() {
            runCatching { processBuilder.command(listOf("docker", "stop", containerName)).start().waitFor() }
        }

        fun removeContainerIfExists() {
            runCatching { processBuilder.command(listOf("docker", "rm", "-f", containerName)).start().waitFor() }
        }

        beforeSpec {
            if (!dockerAvailable()) error("Docker not available; cannot run e2e 'run' command test")
            removeContainerIfExists()

            wdeeProcess =
                TestUtils.wdeeCommandHandlerAndLogger(
                    processBuilder = processBuilder,
                    wdeeBuiltJarPath = wdeeBuiltJar.absolutePath,
                    command = "run",
                    wdeeConfigFilePath = configFile.absolutePath,
                    projectDir = projectDir,
                )

            eventually(eventuallyWait) {
                wdeeRunning()
            }
        }

        afterSpec {
            stopContainer()
            runCatching { wdeeProcess.destroy() }
        }

        fun endpoint(path: String) = "http://localhost:$port$path"

        test("Kotlin no dependency transformer") {
            HttpUtils.get(endpoint("/ResponseTransformerExtensionNoDependenciesKotlin")) shouldBe
                "Response from ResponseTransformerExtensionNoDependenciesKotlin"
        }
        test("Java no dependency transformer") {
            HttpUtils.get(endpoint("/ResponseTransformerExtensionNoDependenciesJava")) shouldBe
                "Response from ResponseTransformerExtensionNoDependenciesJava"
        }
        test("Kotlin with dependency transformer") {
            HttpUtils.get(endpoint("/ResponseTransformerExtensionWithDependenciesKotlin")) shouldBe
                "Response from ResponseTransformerExtensionWithDependenciesKotlin using StringUtils from Apache Commons Lang3"
        }
    })
