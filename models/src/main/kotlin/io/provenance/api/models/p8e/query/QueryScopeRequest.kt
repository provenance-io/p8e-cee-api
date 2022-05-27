package io.provenance.api.models.p8e.query

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.eos.ObjectStoreConfig
import io.provenance.api.models.p8e.ProvenanceConfig
import java.util.UUID

data class QueryScopeRequest(
    val scopeUuid: UUID,
    val provenanceConfig: ProvenanceConfig,
    val account: AccountInfo = AccountInfo(),
    val client: ObjectStoreConfig,
    val height: Long? = null,
    val hydrate: Boolean = false,
)
