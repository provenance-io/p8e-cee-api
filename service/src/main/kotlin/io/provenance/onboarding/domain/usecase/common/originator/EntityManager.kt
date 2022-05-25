package io.provenance.onboarding.domain.usecase.common.originator

import io.provenance.api.models.p8e.AudienceKeyPair
import io.provenance.api.models.p8e.PermissionInfo
import io.provenance.core.KeyType
import io.provenance.core.OriginatorManager
import io.provenance.onboarding.frameworks.config.ProvenanceProperties
import io.provenance.onboarding.frameworks.config.VaultProperties
import io.provenance.plugins.vault.VaultPlugin
import io.provenance.plugins.vault.VaultSpec
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.io.File
import java.util.UUID

@Component
class EntityManager(
    private val vaultProperties: VaultProperties,
    private val provenanceProperties: ProvenanceProperties,
) {
    private val log = KotlinLogging.logger { }
    private var manager: OriginatorManager = OriginatorManager()
    private lateinit var token: String

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

    fun getEntity(args: UUID) =
        manager.get(args, VaultSpec(args, "${vaultProperties.address}/$args", token))

    fun hydrateKeys(permissions: PermissionInfo?): Set<AudienceKeyPair> {
        val additionalAudiences: MutableSet<AudienceKeyPair> = mutableSetOf()

        permissions?.audiences?.forEach {
            it.uuid?.let { entity ->
                val originator = getEntity(entity)
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

        if (permissions?.permissionPortfolioManager == true) additionalAudiences.add(getMemberKeyPair(DefaultAudience.PORTFOLIO_MANAGER))
        if (permissions?.permissionDart == true) additionalAudiences.add(getMemberKeyPair(DefaultAudience.DART))

        return additionalAudiences
    }

    private fun getMemberKeyPair(audience: DefaultAudience): AudienceKeyPair =
        provenanceProperties.members.firstOrNull { it.name == audience }?.let {
            val entity = getEntity(it.uuid)
            AudienceKeyPair(
                entity.keys[KeyType.ENCRYPTION_PUBLIC_KEY].toString(),
                entity.keys[KeyType.SIGNING_PUBLIC_KEY].toString(),
            )
        } ?: throw IllegalStateException("Failed to find requested aud")
}
