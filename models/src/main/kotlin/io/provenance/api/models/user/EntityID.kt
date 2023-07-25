package io.provenance.api.models.user

import com.fasterxml.jackson.annotation.JsonCreator
import java.util.UUID

sealed class EntityID {
    companion object {
        @JsonCreator
        @JvmStatic
        fun of(s: String): EntityID = runCatching { UserUUID(UUID.fromString(s)) }.getOrElse { Address(s) }
    }

    override fun toString() = when (this) {
        is Address -> value
        is UserUUID -> value.toString()
    }
}
data class UserUUID(val value: UUID) : EntityID()
data class Address(val value: String) : EntityID()
