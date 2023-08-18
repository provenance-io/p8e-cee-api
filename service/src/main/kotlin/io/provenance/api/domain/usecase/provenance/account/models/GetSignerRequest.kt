package io.provenance.api.domain.usecase.provenance.account.models

import io.provenance.api.models.entity.Entity
import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.entity.KongConsumer
import io.provenance.api.models.entity.MemberUUID
import java.util.UUID

sealed interface GetSignerRequest {

    companion object {
        operator fun invoke(entity: Entity, account: AccountInfo): GetSignerRequest = when (entity) {
            is KongConsumer -> GetSignerByAddressRequest(entity.id, account)
            is MemberUUID -> GetSignerByUUIDRequest(entity.value, account)
        }
    }

    val account: AccountInfo

    val entity: String
}

data class GetSignerByUUIDRequest(
    val uuid: UUID,
    override val account: AccountInfo
) : GetSignerRequest {
    override val entity: String
        get() = uuid.toString()
}

data class GetSignerByAddressRequest(
    val address: String,
    override val account: AccountInfo
) : GetSignerRequest {
    override val entity: String
        get() = address
}
