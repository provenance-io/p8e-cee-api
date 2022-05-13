package io.provenance.onboarding.frameworks.objectStore

import com.google.common.io.BaseEncoding
import io.provenance.api.models.p8e.Audience
import io.provenance.api.models.p8e.AudienceKeyPair
import io.provenance.api.models.p8e.PermissionInfo
import io.provenance.core.KeyType
import io.provenance.onboarding.domain.usecase.common.originator.GetOriginator
import io.provenance.onboarding.frameworks.config.ServiceKeysProperties
import io.provenance.scope.encryption.ecies.ECUtils
import org.springframework.stereotype.Component
import java.security.PublicKey

@Component
class AudienceKeyManager(
    private val serviceKeys: ServiceKeysProperties,
    private val getOriginator: GetOriginator,
) {
    // TODO: These keys belong in vault
    // This simple map of default audience members could be extended into:
    // 1) a mutable map where each new audience member is stored in memory after the first use to reduce calls to decode/convert string to key on subsequent use
    // 2) a service that fetches audience keys from an external storage location much like the OriginatorManager
    private val audienceMembers: Map<DefaultAudience, PublicKey> = mapOf(
        DefaultAudience.DART to ECUtils.convertBytesToPublicKey(BaseEncoding.base64().decode(serviceKeys.dart)),
        DefaultAudience.PORTFOLIO_MANAGER to ECUtils.convertBytesToPublicKey(BaseEncoding.base64().decode(serviceKeys.portfolioManager)),
    )

    fun get(audience: DefaultAudience): PublicKey {
        return audienceMembers[audience] ?: throw IllegalArgumentException("${audience.name} is not a supported audience.")
    }

    fun hydrateKeys(permissions: PermissionInfo?): Set<AudienceKeyPair> {
        val additionalAudiences: MutableSet<AudienceKeyPair> = mutableSetOf()

        permissions?.audiences?.forEach {
            it.uuid?.let { entity ->
                val originator = getOriginator.executeBlocking(entity)
                additionalAudiences.add(
                    AudienceKeyPair(
                        originator.keys[KeyType.ENCRYPTION_PUBLIC_KEY].toString(),
                        originator.keys[KeyType.SIGNING_PUBLIC_KEY].toString(),
                    )
                )
            }
        }

        return additionalAudiences
    }
}
