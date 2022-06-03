package io.provenance.api.domain.usecase.provenance.contracts.status.models

import java.util.UUID

data class GetStatusOfClassificationRequest(
    val uuid: UUID,
    val assetUuid: UUID,
    val contractName: String,
    val chainId: String,
    val nodeEndpoint: String,
)
