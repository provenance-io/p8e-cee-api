package io.provenance.api.frameworks.config

import io.provenance.api.domain.usecase.common.originator.DefaultAudience
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
        lateinit var id: String

        @NotNull
        lateinit var name: DefaultAudience
    }
}
