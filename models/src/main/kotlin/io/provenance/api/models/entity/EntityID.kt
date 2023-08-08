package io.provenance.api.models.entity

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

    override fun toString() = when (this) {
        is Address -> value
        is UserUUID -> value.toString()
        is Consumer -> username ?: customId ?: "Anonymous"
    }
}
data class Consumer(val id: String?, val username: String?, val customId: String?) : EntityID() {
    override fun toString(): String = username ?: customId ?: "Anonymous"
}
data class UserUUID(val value: UUID) : EntityID() { override fun toString(): String = value.toString() }
data class Address(val value: String) : EntityID() { override fun toString(): String = value }
