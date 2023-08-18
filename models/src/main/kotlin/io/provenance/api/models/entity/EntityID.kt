package io.provenance.api.models.entity

import java.util.UUID

sealed interface Entity { val id: String }

/**
 * Kong consumer
 *
 * @property id
 * @property username - will be null for consumers created for delegated keys
 * @property customId - contains granter address for delegated keys
 * @constructor Create empty Kong consumer
 */
data class KongConsumer(
    val entityId: String,
    val username: String?,
    val customId: String?,
) : Entity {
    override val id: String
        get() = customId ?: username ?: entityId
}

data class MemberUUID(val value: UUID) : Entity {
    override val id: String
        get() = value.toString()
}
