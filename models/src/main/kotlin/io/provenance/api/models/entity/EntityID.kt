package io.provenance.api.models.entity

import java.util.UUID

sealed interface Entity

/**
 * Kong consumer
 *
 * @property id
 * @property username - will be null for consumers created for delegated keys
 * @property customId - contains granter address for delegated keys
 * @constructor Create empty Kong consumer
 */
data class KongConsumer(
    val id: String,
    val username: String?,
    val customId: String,
) : Entity

data class MemberUUID(val value: UUID) : Entity
