package io.provenance.api.frameworks.web.external.provenance

import io.provenance.api.domain.usecase.provenance.contracts.classify.ClassifyAsset
import io.provenance.api.domain.usecase.provenance.contracts.classify.models.ClassifyAssetRequestWrapper
import io.provenance.api.domain.usecase.provenance.contracts.fees.GetFeesForAsset
import io.provenance.api.domain.usecase.provenance.contracts.fees.models.GetFeesForAssetRequest
import io.provenance.api.domain.usecase.provenance.contracts.status.GetClassificationStatus
import io.provenance.api.domain.usecase.provenance.contracts.status.models.GetStatusOfClassificationRequest
import io.provenance.api.domain.usecase.provenance.contracts.verify.VerifyAsset
import io.provenance.api.domain.usecase.provenance.contracts.verify.models.VerifyAssetRequestWrapper
import io.provenance.api.domain.usecase.provenance.query.QueryScope
import io.provenance.api.domain.usecase.provenance.tx.create.CreateTx
import io.provenance.api.domain.usecase.provenance.tx.create.models.CreateTxRequestWrapper
import io.provenance.api.domain.usecase.provenance.tx.execute.ExecuteTx
import io.provenance.api.domain.usecase.provenance.tx.execute.models.ExecuteTxRequestWrapper
import io.provenance.api.domain.usecase.provenance.tx.permissions.UpdateScopeDataAccess
import io.provenance.api.domain.usecase.provenance.tx.permissions.models.UpdateScopeDataAccessRequestWrapper
import io.provenance.api.frameworks.web.misc.foldToServerResponse
import io.provenance.api.frameworks.web.misc.getUser
import io.provenance.api.models.p8e.query.QueryScopeRequest
import io.provenance.scope.util.toUuid
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody

@Component
class ProvenanceHandler(
    private val executeTx: ExecuteTx,
    private val createTx: CreateTx,
    private val queryScope: QueryScope,
    private val classifyAsset: ClassifyAsset,
    private val verifyAsset: VerifyAsset,
    private val getFees: GetFeesForAsset,
    private val getClassificationStatus: GetClassificationStatus,
    private val updateDataAccess: UpdateScopeDataAccess
) {
    suspend fun generateTx(req: ServerRequest): ServerResponse = runCatching {
        createTx.execute(CreateTxRequestWrapper(req.getUser(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun executeTx(req: ServerRequest): ServerResponse = runCatching {
        executeTx.execute(ExecuteTxRequestWrapper(req.getUser(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun queryScope(req: ServerRequest): ServerResponse = runCatching {
        queryScope.execute(
            QueryScopeRequest(
                req.getUser(),
                req.queryParam("scopeUuid").get().toUuid(),
                req.queryParam("chainId").get(),
                req.queryParam("nodeEndpoint").get(),
                req.queryParam("objectStoreUrl").get(),
            )
        )
    }.foldToServerResponse()

    suspend fun classifyAsset(req: ServerRequest): ServerResponse = runCatching {
        classifyAsset.execute(ClassifyAssetRequestWrapper(req.getUser(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun verifyAsset(req: ServerRequest): ServerResponse = runCatching {
        verifyAsset.execute(VerifyAssetRequestWrapper(req.getUser(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun getFees(req: ServerRequest): ServerResponse = runCatching {
        getFees.execute(
            GetFeesForAssetRequest(
                req.getUser(),
                req.queryParam("contractName").get(),
                req.queryParam("assetType").get(),
                req.queryParam("chainId").get(),
                req.queryParam("nodeEndpoint").get()
            )
        )
    }.foldToServerResponse()

    suspend fun getClassificationStatus(req: ServerRequest): ServerResponse = runCatching {
        getClassificationStatus.execute(
            GetStatusOfClassificationRequest(
                req.getUser(),
                req.queryParam("assetUuid").get().toUuid(),
                req.queryParam("contractName").get(),
                req.queryParam("chainId").get(),
                req.queryParam("nodeEndpoint").get()
            )
        )
    }.foldToServerResponse()

    suspend fun updateDataAccess(req: ServerRequest): ServerResponse = runCatching {
        updateDataAccess.execute(
            UpdateScopeDataAccessRequestWrapper(
                req.getUser(),
                req.awaitBody()
            )
        )
    }.foldToServerResponse()
}
