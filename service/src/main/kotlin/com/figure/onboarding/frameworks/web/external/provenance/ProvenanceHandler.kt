package com.figure.onboarding.frameworks.web.external.provenance

import com.figure.onboarding.domain.usecase.provenance.tx.ExecuteTx
import com.figure.onboarding.domain.usecase.provenance.specifications.WriteSpecifications
import com.figure.onboarding.domain.usecase.provenance.tx.CreateTx
import com.figure.onboarding.domain.usecase.provenance.tx.CreateTxOnboardAsset
import com.figure.onboarding.frameworks.web.misc.foldToServerResponse
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody

@Component
class ProvenanceHandler(
    private val executeTx: ExecuteTx,
    private val createTx: CreateTx,
    private val createTxAndOnboardAsset: CreateTxOnboardAsset,
    private val writeSpecifications: WriteSpecifications,
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

    suspend fun writeSpecifications(req: ServerRequest): ServerResponse = runCatching {
        writeSpecifications.execute(req.awaitBody())
    }.foldToServerResponse()
}
