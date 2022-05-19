package io.provenance.onboarding.frameworks.config

import io.provenance.onboarding.domain.usecase.common.originator.DefaultAudience
import java.util.UUID
import javax.validation.constraints.NotNull
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "p8e")
@Validated
class ProvenanceProperties {
    var mainnet: Boolean = false

    var members: List<Member> = emptyList()

    class Member {
        @NotNull
        lateinit var uuid: UUID

        @NotNull
        lateinit var name: DefaultAudience
    }
}
