package e2e

import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.OutputFrame
import org.testcontainers.utility.DockerImageName
import org.testcontainers.utility.MountableFile
import utils.HttpUtils
import utils.TestUtils
import java.io.File
import kotlin.time.Duration.Companion.seconds

class E2eJarBuild :
    FunSpec({

        val projectDir = File(".").canonicalFile
        val e2eFilesDir = TestUtils.getConfigFileFromResources("e2e-resources")
        val configFile = File(e2eFilesDir, "wdee-config.yaml")
        val wiremockMappingsDir = File(e2eFilesDir, "mappings")
        val wiremockFilesDir = File(e2eFilesDir, "__files")
        val wdeeBuiltJar = File(projectDir, "build/libs/wiremock-docker-easy-extensions.jar")
        val port = 8080
        val processBuilder = ProcessBuilder()
        val eventuallyWait = 30.seconds
        lateinit var wdeeProcess: Process
        lateinit var wiremockContainer: GenericContainer<*>

        fun locateBuiltExtensionJar(): File = File(projectDir, "build/extensions/wiremock-extensions-bundled.jar")

        val hostResolver = { "http://${wiremockContainer.host}:${wiremockContainer.getMappedPort(port)}" }

        fun wiremockRunning() = HttpUtils.get("${hostResolver()}/__admin/health")

        beforeSpec {
            wdeeProcess =
                TestUtils.wdeeCommandHandlerAndLogger(
                    processBuilder = processBuilder,
                    wdeeBuiltJarPath = wdeeBuiltJar.absolutePath,
                    command = "build",
                    wdeeConfigFilePath = configFile.absolutePath,
                    projectDir = projectDir,
                )
            eventually(eventuallyWait) {
                wdeeProcess.waitFor() == 0
            }

            val extensionJar = locateBuiltExtensionJar()

            wiremockContainer =
                GenericContainer(DockerImageName.parse("wiremock/wiremock:3.13.2"))
                    .withExposedPorts(8080)
                    .withCopyFileToContainer(MountableFile.forHostPath(wiremockMappingsDir.absolutePath), "/home/wiremock/mappings")
                    .withCopyFileToContainer(MountableFile.forHostPath(wiremockFilesDir.absolutePath), "/home/wiremock/__files")
                    .withCopyFileToContainer(MountableFile.forHostPath(extensionJar.absolutePath), "/var/wiremock/extensions/")
                    .withLogConsumer { frame: OutputFrame ->
                        val txt = frame.utf8String?.trimEnd().orEmpty()
                        if (txt.isNotEmpty()) {
                            println("[wiremockContainer] $txt")
                        }
                    }
            wiremockContainer.start()

            eventually(eventuallyWait) {
                wiremockRunning()
            }
        }

        afterSpec {
            runCatching {
                wiremockContainer.stop()
                runCatching { wdeeProcess.destroy() }
            }
        }

        fun endpoint(path: String) = "${hostResolver()}$path"

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
        test("No Extension stub") {
            HttpUtils.get(endpoint("/noExtension")) shouldBe
                "No Extension"
        }
    })
