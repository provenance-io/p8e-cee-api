package io.provenance.api.models.user

import java.util.UUID

sealed class UserID {
    override fun toString() = when (this) {
        is UserAddress -> value
        is UserUUID -> value.toString()
    }
}
data class UserUUID(val value: UUID) : UserID()
data class UserAddress(val value: String) : UserID()
