package io.provenance.api.models.user

import com.fasterxml.jackson.annotation.JsonCreator
import java.util.UUID

sealed class EntityID {
    companion object {
        @JsonCreator
        @JvmStatic
        fun fromString(string: String): EntityID = runCatching {
            UserUUID(UUID.fromString(string))
        }.getOrElse { Address(string) }
    }
}
data class UserUUID(val value: UUID) : EntityID() { override fun toString(): String = value.toString() }
data class Address(val value: String) : EntityID() { override fun toString(): String = value }
