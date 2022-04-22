package io.provenance.onboarding.frameworks.web.external.provenance

import io.provenance.onboarding.domain.usecase.provenance.tx.CreateTx
import io.provenance.onboarding.domain.usecase.provenance.tx.CreateTxOnboardAsset
import io.provenance.onboarding.domain.usecase.provenance.tx.ExecuteTx
import io.provenance.onboarding.frameworks.web.misc.foldToServerResponse
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody

@Component
class ProvenanceHandler(
    private val executeTx: ExecuteTx,
    private val createTx: CreateTx,
    private val createTxAndOnboardAsset: CreateTxOnboardAsset
) {
    suspend fun createTxAndOnboard(req: ServerRequest): ServerResponse = runCatching {
        createTxAndOnboardAsset.execute(req.awaitBody())
    }.foldToServerResponse()

    suspend fun generateTx(req: ServerRequest): ServerResponse = runCatching {
        createTx.execute(req.awaitBody())
    }.foldToServerResponse()

    suspend fun executeTx(req: ServerRequest): ServerResponse = runCatching {
        executeTx.execute(req.awaitBody())
    }.foldToServerResponse()
}
