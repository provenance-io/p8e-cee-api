package io.provenance.api.domain.usecase.common.originator

import io.provenance.api.domain.usecase.common.originator.models.KeyManagementConfigWrapper
import io.provenance.api.frameworks.config.ProvenanceProperties
import io.provenance.api.frameworks.config.VaultProperties
import io.provenance.api.models.account.KeyManagementConfig
import io.provenance.api.models.p8e.AudienceKeyPair
import io.provenance.api.models.p8e.PermissionInfo
import io.provenance.core.KeyType
import io.provenance.core.Originator
import io.provenance.core.OriginatorManager
import io.provenance.core.Plugin
import io.provenance.plugins.vault.VaultSpec
import java.io.File
import org.springframework.stereotype.Component
import kotlin.reflect.full.createInstance

@Component
class EntityManager(
    private val vaultProperties: VaultProperties,
    private val provenanceProperties: ProvenanceProperties,
) {
    private var manager: OriginatorManager = OriginatorManager()
    private var tokenMap = mutableMapOf<KeyManagementConfig, String>()

    fun getEntity(args: KeyManagementConfigWrapper): Originator {

        val config = args.config ?: KeyManagementConfig(
            vaultProperties.address,
            vaultProperties.tokenPath,
        )

        val token = fetchToken(config)
        return manager.get(args.uuid, VaultSpec(args.uuid, "${config.address}/${args.uuid}", token))
    }

    fun hydrateKeys(permissions: PermissionInfo?, keyManagementConfig: KeyManagementConfig? = null): Set<AudienceKeyPair> {

        val config = keyManagementConfig ?: KeyManagementConfig(
            vaultProperties.address,
            vaultProperties.tokenPath,
        )

        val additionalAudiences: MutableSet<AudienceKeyPair> = mutableSetOf()

        permissions?.audiences?.forEach {
            it.uuid?.let { entity ->
                val originator = getEntity(KeyManagementConfigWrapper(entity, config))
                additionalAudiences.add(
                    AudienceKeyPair(
                        originator.keys[KeyType.ENCRYPTION_PUBLIC_KEY].toString(),
                        originator.keys[KeyType.SIGNING_PUBLIC_KEY].toString(),
                    )
                )
            } ?: apply {
                it.keys?.let { keys ->
                    additionalAudiences.add(keys)
                }
            }
        }

        if (permissions?.permissionPortfolioManager == true) additionalAudiences.add(getMemberKeyPair(DefaultAudience.PORTFOLIO_MANAGER, config))
        if (permissions?.permissionDart == true) additionalAudiences.add(getMemberKeyPair(DefaultAudience.DART, config))

        return additionalAudiences
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun fetchToken(config: KeyManagementConfig): String {

        if (tokenMap.containsKey(config)) {
            return tokenMap[config]!!
        }

        val plugin = Class.forName(config.plugin).asSubclass(Plugin::class.java).kotlin.createInstance()
        manager.register(plugin)

        val tokenPath = if (File(config.tokenPath).exists()) {
            File(config.tokenPath)
        } else {
            File(System.getProperty("user.home")).resolve(config.tokenPath)
        }

        tokenMap[config] = tokenPath.readText(Charsets.UTF_8)

        return tokenMap[config]!!
    }

    private fun getMemberKeyPair(audience: DefaultAudience, keyManagementConfig: KeyManagementConfig): AudienceKeyPair =
        provenanceProperties.members.firstOrNull { it.name == audience }?.let {
            val entity = getEntity(KeyManagementConfigWrapper(it.uuid, keyManagementConfig))
            AudienceKeyPair(
                entity.keys[KeyType.ENCRYPTION_PUBLIC_KEY].toString(),
                entity.keys[KeyType.SIGNING_PUBLIC_KEY].toString(),
            )
        } ?: throw IllegalStateException("Failed to find requested aud")
}
