package io.provenance.api.domain.usecase.common.originator

import io.provenance.api.domain.usecase.common.originator.models.KeyManagementConfigWrapper
import io.provenance.api.frameworks.config.ProvenanceProperties
import io.provenance.api.frameworks.config.VaultProperties
import io.provenance.api.models.account.KeyManagementConfig
import io.provenance.api.models.account.Participant
import io.provenance.api.models.p8e.AudienceKeyPair
import io.provenance.api.models.p8e.PermissionInfo
import io.provenance.core.KeyEntityManager
import io.provenance.core.Plugin
import io.provenance.entity.KeyEntity
import io.provenance.entity.KeyType
import io.provenance.plugins.vault.VaultConfig
import io.provenance.scope.objectstore.util.toHex
import kotlin.reflect.full.createInstance
import org.springframework.stereotype.Component

@Component
class EntityManager(
    private val vaultProperties: VaultProperties,
    private val provenanceProperties: ProvenanceProperties
) {
    private var manager = KeyEntityManager()

    fun getEntity(args: KeyManagementConfigWrapper): KeyEntity {
        val config = args.config ?: KeyManagementConfig(
            pluginConfig = VaultConfig(
                "${vaultProperties.address}/${args.entityId}",
                vaultProperties.tokenPath,
            )
        )

        val plugin = Class.forName(config.plugin).asSubclass(Plugin::class.java).kotlin.createInstance()
        manager.register(plugin)
        return manager.get(args.entityId, config.pluginConfig)
    }

    fun hydrateKeys(permissions: PermissionInfo?, participants: List<Participant> = emptyList(), keyManagementConfig: KeyManagementConfig? = null): Set<AudienceKeyPair> {
        val additionalAudiences: MutableSet<AudienceKeyPair> = mutableSetOf()

        // Populate participants into the audience list
        participants.forEach { participant ->
            val keyEntity = getEntity(KeyManagementConfigWrapper(participant.uuid.toString(), keyManagementConfig))

            additionalAudiences.add(
                AudienceKeyPair(
                    keyEntity.publicKey(KeyType.ENCRYPTION).toHex(),
                    keyEntity.publicKey(KeyType.SIGNING).toHex(),
                )
            )
        }

        // Populate the audiences into the audience list
        permissions?.audiences?.forEach {
            it.uuid?.let { uuid ->
                val keyEntity = getEntity(KeyManagementConfigWrapper(uuid.toString(), keyManagementConfig))

                additionalAudiences.add(
                    AudienceKeyPair(
                        keyEntity.publicKey(KeyType.ENCRYPTION).toHex(),
                        keyEntity.publicKey(KeyType.SIGNING).toHex(),
                    )
                )
            } ?: apply {
                it.keys?.let { keys ->
                    additionalAudiences.add(keys)
                }
            }
        }

        // DART and Portfolio specific permissioning
        if (permissions?.permissionPortfolioManager == true) additionalAudiences.add(getMemberKeyPair(DefaultAudience.PORTFOLIO_MANAGER, keyManagementConfig))
        if (permissions?.permissionDart == true) additionalAudiences.add(getMemberKeyPair(DefaultAudience.DART, keyManagementConfig))

        return additionalAudiences
    }

    fun hydrateKeys(addresses: List<String>, keyManagementConfig: KeyManagementConfig? = null): Set<AudienceKeyPair> =
        addresses.map {
            getEntity(KeyManagementConfigWrapper(it, keyManagementConfig)).let { entity ->
                AudienceKeyPair(
                    entity.publicKey(KeyType.ENCRYPTION).toHex(),
                    entity.publicKey(KeyType.SIGNING).toHex()
                )
            }
        }.toSet()

    private fun getMemberKeyPair(audience: DefaultAudience, keyManagementConfig: KeyManagementConfig? = null): AudienceKeyPair =
        provenanceProperties.members.firstOrNull { it.name == audience }?.let {
            val entity = getEntity(KeyManagementConfigWrapper(it.id, keyManagementConfig))
            AudienceKeyPair(
                entity.publicKey(KeyType.ENCRYPTION).toHex(),
                entity.publicKey(KeyType.SIGNING).toHex(),
            )
        } ?: throw IllegalStateException("Failed to find requested audience")
}
