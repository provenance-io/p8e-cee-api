package io.provenance.api.domain.usecase.provenance.account.models

import io.provenance.api.models.user.UserAddress
import io.provenance.api.models.user.UserID
import io.provenance.api.models.user.UserUUID
import io.provenance.api.models.account.AccountInfo
import java.util.UUID

sealed interface GetSignerRequest {

    companion object {
        operator fun invoke(id: UserID, account: AccountInfo): GetSignerRequest = when (id) {
            is UserAddress -> GetSignerByAddressRequest(id.value, account)
            is UserUUID -> GetSignerByUUIDRequest(id.value, account)
        }
    }

    val account: AccountInfo

    val entity: String
        get() = when (this) {
            is GetSignerByAddressRequest -> address
            is GetSignerByUUIDRequest -> uuid.toString()
        }
}

data class GetSignerByUUIDRequest(
    val uuid: UUID,
    override val account: AccountInfo
) : GetSignerRequest

data class GetSignerByAddressRequest(
    val address: String,
    override val account: AccountInfo
) : GetSignerRequest
