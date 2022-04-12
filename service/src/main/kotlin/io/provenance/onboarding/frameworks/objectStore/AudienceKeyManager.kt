package io.provenance.onboarding.frameworks.objectStore

import io.provenance.onboarding.frameworks.config.ServiceKeysProperties
import com.google.common.io.BaseEncoding
import io.provenance.scope.encryption.ecies.ECUtils
import java.security.PublicKey
import org.springframework.stereotype.Component

@Component
class AudienceKeyManager(
    private val serviceKeys: ServiceKeysProperties
) {
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
}
