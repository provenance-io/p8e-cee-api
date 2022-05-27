package io.provenance.api.frameworks.web.external.provenance

import io.provenance.api.domain.usecase.provenance.query.QueryScope
import io.provenance.api.domain.usecase.provenance.query.models.QueryScopeRequestWrapper
import io.provenance.api.domain.usecase.provenance.tx.CreateTx
import io.provenance.api.domain.usecase.provenance.tx.ExecuteTx
import io.provenance.api.domain.usecase.provenance.tx.model.CreateTxRequestWrapper
import io.provenance.api.domain.usecase.provenance.tx.model.ExecuteTxRequestWrapper
import io.provenance.api.frameworks.web.misc.foldToServerResponse
import io.provenance.api.frameworks.web.misc.getUser
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody

@Component
class ProvenanceHandler(
    private val executeTx: ExecuteTx,
    private val createTx: CreateTx,
    private val queryScope: QueryScope
) {
    suspend fun generateTx(req: ServerRequest): ServerResponse = runCatching {
        createTx.execute(CreateTxRequestWrapper(req.getUser(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun executeTx(req: ServerRequest): ServerResponse = runCatching {
        executeTx.execute(ExecuteTxRequestWrapper(req.getUser(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun queryScope(req: ServerRequest): ServerResponse = runCatching {
        queryScope.execute(QueryScopeRequestWrapper(req.getUser(), req.awaitBody()))
    }.foldToServerResponse()
}
