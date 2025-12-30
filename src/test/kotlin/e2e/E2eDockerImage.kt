package e2e

import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.OutputFrame
import org.testcontainers.images.builder.ImageFromDockerfile
import org.testcontainers.utility.MountableFile
import utils.HttpUtils
import utils.TestUtils
import java.io.File
import kotlin.time.Duration.Companion.minutes

class E2eDockerImage :
    FunSpec({
        val projectDir = File(".").canonicalFile
        val e2eFilesDir = TestUtils.getConfigFileFromResources("e2e-resources")
        val port = 8080
        val eventuallyWait = 3.minutes
        lateinit var wiremockContainer: GenericContainer<*>

        val hostResolver = { "http://${wiremockContainer.host}:${wiremockContainer.getMappedPort(port)}" }

        fun wiremockRunning() = HttpUtils.get("${hostResolver()}/__admin/health")

        beforeSpec {
            val dockerImage =
                ImageFromDockerfile()
                    .withFileFromFile(".", projectDir)

            wiremockContainer =
                GenericContainer(dockerImage)
                    .withExposedPorts(port)
                    .withCopyFileToContainer(MountableFile.forHostPath(e2eFilesDir.absolutePath), "/home/config/e2e-resources")
                    .withLogConsumer { frame: OutputFrame ->
                        val txt = frame.utf8String?.trimEnd().orEmpty()
                        if (txt.isNotEmpty()) {
                            println("[wiremockDockerImage] $txt")
                        }
                    }
            wiremockContainer.start()

            eventually(eventuallyWait) {
                wiremockRunning()
            }
        }

        afterSpec {
            runCatching { wiremockContainer.stop() }
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
