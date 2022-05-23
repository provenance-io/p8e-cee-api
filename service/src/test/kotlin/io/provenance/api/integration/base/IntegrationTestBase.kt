package io.provenance.api.integration.base

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.WordSpec
import io.kotest.extensions.spring.SpringExtension
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.DockerComposeContainer

@ActiveProfiles("development")
@SpringBootTest
open class IntegrationTestBase(body: WordSpec.() -> Unit = {}) : WordSpec(body) {

    override fun extensions(): List<Extension> = listOf(SpringExtension)

    override suspend fun beforeSpec(spec: Spec) {

        instance.stop()
        instance.start()

        withContext(Dispatchers.IO) {
            ProcessBuilder().command("./dc.sh")
                .directory(File("src/test/resources/"))
                .redirectError( ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .start()
                .waitFor(10, TimeUnit.SECONDS)
        }

        super.beforeSpec(spec)
    }

    override fun afterSpec(f: suspend (Spec) -> Unit) {
        instance.stop()
        super.afterSpec(f)
    }

    companion object {
        class KDockerComposeContainer(file: File) : DockerComposeContainer<KDockerComposeContainer>(file)

        internal val instance: KDockerComposeContainer by lazy { defineDockerCompose() }
        private fun defineDockerCompose() = KDockerComposeContainer(File("src/test/resources/dependencies.yaml"))
    }
}
