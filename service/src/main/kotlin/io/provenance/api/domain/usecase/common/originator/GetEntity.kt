package io.provenance.api.domain.usecase.common.originator

import io.provenance.core.Originator
import io.provenance.core.OriginatorManager
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.common.originator.models.KeyManagementConfigWrapper
import io.provenance.api.models.account.KeyManagementConfig
import io.provenance.core.Plugin
import io.provenance.plugins.vault.VaultPlugin
import io.provenance.plugins.vault.VaultSpec
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.io.File
import kotlin.reflect.full.createInstance

@Component
class GetEntity : AbstractUseCase<KeyManagementConfigWrapper, Originator>() {

    private val log = KotlinLogging.logger { }
    private var manager: OriginatorManager = OriginatorManager()
    private var tokenMap = mutableMapOf<KeyManagementConfig, String>()


    override suspend fun execute(args: KeyManagementConfigWrapper): Originator {
        val token = fetchToken(args.config)
        return manager.get(args.uuid, VaultSpec(args.uuid, "${args.config.address}/$args", token))
    }

    private fun fetchToken(config: KeyManagementConfig): String {

        if (tokenMap.containsKey(config)) {
            return tokenMap[config]!!
        }

        val plugin = Class.forName(config.plugin).asSubclass(Plugin::class.java).kotlin.createInstance()
        manager.register(plugin)

        val tokenPath = if (System.getenv("ENVIRONMENT").isNullOrBlank()) {
            log.info("Retrieving token from ${System.getProperty("user.home") + config.tokenPath}")
            File(System.getProperty("user.home")).resolve(config.tokenPath)
        } else {
            log.info("Retrieving token from ${config.tokenPath} on environment: ${config.tokenPath}}")
            File(config.tokenPath)
        }

        tokenMap[config] = tokenPath.readText(Charsets.UTF_8)

        return tokenMap[config]!!
    }
}
