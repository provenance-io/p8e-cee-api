package io.provenance.api.models.p8e.query

import io.provenance.api.models.user.UserID
import java.util.UUID

data class QueryScopeRequest(
    val userID: UserID,
    val scopeUuid: UUID,
    val chainId: String,
    val nodeEndpoint: String,
    val objectStoreUrl: String,
    val height: Long? = null,
    val hydrate: Boolean = false,
)
