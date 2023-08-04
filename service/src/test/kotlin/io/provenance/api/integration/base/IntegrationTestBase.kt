package io.provenance.api.integration.base

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.WordSpec
import io.kotest.extensions.spring.SpringExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.testcontainers.containers.DockerComposeContainer
import java.io.File
import java.util.concurrent.TimeUnit

abstract class IntegrationTestBase(body: WordSpec.() -> Unit = {}) : WordSpec(body) {

    override fun extensions(): List<Extension> = listOf(SpringExtension)

    override suspend fun beforeSpec(spec: Spec) {
        instance.stop()
        instance.start()

        withContext(Dispatchers.IO) {
            val process: Process = ProcessBuilder()
                .directory(File("src/test/resources/"))
                .command("./integration_test_setup.sh")
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .start()

            process.waitFor(60, TimeUnit.SECONDS)

            if (process.exitValue() != 0) {
                throw IllegalStateException("Integration test environment did NOT setup successfully!")
            }
        }

        super.beforeSpec(spec)
    }

    override fun afterSpec(f: suspend (Spec) -> Unit) {
        instance.stop()
        super.afterSpec(f)
    }

    companion object {
        class KDockerComposeContainer(file: File) : DockerComposeContainer<KDockerComposeContainer>(file)

        internal val instance: KDockerComposeContainer by lazy {
            defineDockerCompose()
        }

        /*
            Ideally these should be replaced with the container states commented out below.
            For whatever reason when running on terminal, the containers could not be found.
            Related GitHub issue: https://github.com/testcontainers/testcontainers-java/issues/4281
        */
        const val OBJECT_STORE_ADDRESS = "grpc://localhost:9993"
        const val VAULT_ADDRESS = "http://localhost:8200/v1/kv2_originations/data/originators"
        const val PROVENANCE_ADDRESS = "grpc://localhost:9090"

        // val objectStoreContainer: ContainerState by lazy { instance.getContainerByServiceName("object-store_1").get() }
        // val vaultContainer: ContainerState by lazy { instance.getContainerByServiceName("vault_1").get() }
        private fun defineDockerCompose() = KDockerComposeContainer(File("src/test/resources/dependencies.yaml"))
    }
}
