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
import io.provenance.plugins.vault.VaultSpec
import io.provenance.scope.objectstore.util.toHex
import java.util.UUID
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
            pluginSpec = VaultSpec(
                args.entity,
                "${vaultProperties.address}/${args.entity}",
                vaultProperties.tokenPath,
            )
        )

        val plugin = Class.forName(config.plugin).asSubclass(Plugin::class.java).kotlin.createInstance()
        manager.register(plugin)
        return manager.get(args.entity, config.pluginSpec)
    }

    fun hydrateKeys(permissions: PermissionInfo?, participants: List<Participant> = emptyList(), keyManagementConfig: KeyManagementConfig? = null): Set<AudienceKeyPair> {

        val additionalAudiences: MutableSet<AudienceKeyPair> = mutableSetOf()
        val config = keyManagementConfig ?: KeyManagementConfig(
            vaultProperties.address,
            vaultProperties.tokenPath,
        )

        fun getEntityKeys(uuid: UUID) {
            val originator = getEntity(KeyManagementConfigWrapper(uuid.toString(), config))
            additionalAudiences.add(
                AudienceKeyPair(
                    originator.publicKey(KeyType.ENCRYPTION).toHex(),
                    originator.publicKey(KeyType.SIGNING).toHex(),
                )
            )
        }

        participants.forEach { participant ->
            getEntityKeys(participant.uuid)
        }

        permissions?.audiences?.forEach {
            it.uuid?.let { entity ->
                getEntityKeys(entity)
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

    fun hydrateKeys(addresses: List<String>, keyManagementConfig: KeyManagementConfig? = null): Set<AudienceKeyPair> =
        addresses.map {
            getEntity(KeyManagementConfigWrapper(it, keyManagementConfig)).let { entity ->
                AudienceKeyPair(
                    entity.publicKey(KeyType.ENCRYPTION).toHex(),
                    entity.publicKey(KeyType.SIGNING).toHex()
                )
            }
        }.toSet()

    private fun getMemberKeyPair(audience: DefaultAudience, keyManagementConfig: KeyManagementConfig): AudienceKeyPair =
        provenanceProperties.members.firstOrNull { it.name == audience }?.let {
            val entity = getEntity(KeyManagementConfigWrapper(it.id, keyManagementConfig))
            AudienceKeyPair(
                entity.publicKey(KeyType.ENCRYPTION).toHex(),
                entity.publicKey(KeyType.SIGNING).toHex(),
            )
        } ?: throw IllegalStateException("Failed to find requested audience")
}
