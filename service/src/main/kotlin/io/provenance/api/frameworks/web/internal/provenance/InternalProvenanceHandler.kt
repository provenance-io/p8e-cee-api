package io.provenance.api.frameworks.web.internal.provenance

import io.provenance.api.domain.usecase.provenance.contracts.classify.ClassifyAsset
import io.provenance.api.domain.usecase.provenance.contracts.classify.models.ClassifyAssetRequestWrapper
import io.provenance.api.domain.usecase.provenance.contracts.definitions.GetAssetDefinitions
import io.provenance.api.domain.usecase.provenance.contracts.definitions.models.GetAssetDefinitionsRequest
import io.provenance.api.domain.usecase.provenance.contracts.fees.GetFeesForAsset
import io.provenance.api.domain.usecase.provenance.contracts.fees.models.GetFeesForAssetRequest
import io.provenance.api.domain.usecase.provenance.contracts.status.GetClassificationStatus
import io.provenance.api.domain.usecase.provenance.contracts.status.models.GetStatusOfClassificationRequest
import io.provenance.api.domain.usecase.provenance.contracts.verify.VerifyAsset
import io.provenance.api.domain.usecase.provenance.contracts.verify.models.VerifyAssetRequestWrapper
import io.provenance.api.frameworks.web.misc.foldToServerResponse
import io.provenance.api.frameworks.web.misc.getEntityID
import io.provenance.scope.util.toUuid
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody

@Component
class InternalProvenanceHandler(
    private val classifyAsset: ClassifyAsset,
    private val verifyAsset: VerifyAsset,
    private val getFees: GetFeesForAsset,
    private val getClassificationStatus: GetClassificationStatus,
    private val getAssetDefinitions: GetAssetDefinitions
) {
    suspend fun classifyAsset(req: ServerRequest): ServerResponse = runCatching {
        classifyAsset.execute(ClassifyAssetRequestWrapper(req.getEntityID(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun verifyAsset(req: ServerRequest): ServerResponse = runCatching {
        verifyAsset.execute(VerifyAssetRequestWrapper(req.getEntityID(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun getClassificationStatus(req: ServerRequest): ServerResponse = runCatching {
        getClassificationStatus.execute(
            GetStatusOfClassificationRequest(
                req.getEntityID(),
                req.queryParam("assetUuid").get().toUuid(),
                req.queryParam("assetType").get(),
                req.queryParam("contractName").get(),
                req.queryParam("chainId").get(),
                req.queryParam("nodeEndpoint").get()
            )
        )
    }.foldToServerResponse()

    suspend fun getFees(req: ServerRequest): ServerResponse = runCatching {
        getFees.execute(
            GetFeesForAssetRequest(
                req.getEntityID(),
                req.queryParam("contractName").get(),
                req.queryParam("assetType").get(),
                req.queryParam("chainId").get(),
                req.queryParam("nodeEndpoint").get()
            )
        )
    }.foldToServerResponse()

    suspend fun getAssetDefinitions(req: ServerRequest): ServerResponse = runCatching {
        getAssetDefinitions.execute(
            GetAssetDefinitionsRequest(
                req.queryParam("contractName").get(),
                req.queryParam("chainId").get(),
                req.queryParam("nodeEndpoint").get()
            )
        )
    }.foldToServerResponse()
}
