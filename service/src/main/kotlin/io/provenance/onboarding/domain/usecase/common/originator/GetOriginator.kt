package io.provenance.onboarding.domain.usecase.common.originator

import io.provenance.core.Originator
import io.provenance.core.OriginatorManager
import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.frameworks.config.VaultProperties
import io.provenance.plugins.vault.VaultPlugin
import io.provenance.plugins.vault.VaultSpec
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.io.File
import java.util.UUID

@Component
class GetOriginator(
    private val vaultProperties: VaultProperties
) : AbstractUseCase<UUID, Originator>() {

    private val log = KotlinLogging.logger { }
    private var manager: OriginatorManager = OriginatorManager()
    private var token: String

    init {
        manager.register(VaultPlugin())

        val tokenPath = if (System.getenv("ENVIRONMENT").isNullOrBlank()) {
            log.info("Retrieving token from ${System.getProperty("user.home") + vaultProperties.tokenPath}")
            File(System.getProperty("user.home")).resolve(vaultProperties.tokenPath)
        } else {
            log.info("Retrieving token from ${vaultProperties.tokenPath} on environment: ${vaultProperties.tokenPath}}")
            File(vaultProperties.tokenPath)
        }

        token = tokenPath.readText(Charsets.UTF_8)
    }

    override suspend fun execute(args: UUID) =
        manager.get(args, VaultSpec(args, "${vaultProperties.address}/$args", token))
}
