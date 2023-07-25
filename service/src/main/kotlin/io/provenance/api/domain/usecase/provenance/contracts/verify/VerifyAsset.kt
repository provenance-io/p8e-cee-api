package io.provenance.api.domain.usecase.provenance.contracts.verify

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.domain.usecase.provenance.contracts.verify.models.VerifyAssetRequestWrapper
import io.provenance.api.frameworks.provenance.ProvenanceService
import io.provenance.api.models.p8e.TxResponse
import org.springframework.stereotype.Component
import tech.figure.classification.asset.client.domain.execute.VerifyAssetExecute
import tech.figure.classification.asset.client.domain.model.AssetIdentifier

@Component
class VerifyAsset(
    private val provenanceService: ProvenanceService,
    private val getSigner: GetSigner
) : AbstractUseCase<VerifyAssetRequestWrapper, TxResponse>() {
    override suspend fun execute(args: VerifyAssetRequestWrapper): TxResponse {
        val signer = getSigner.execute(
            GetSignerRequest(
                args.entityID,
                args.request.account,
            )
        )

        val verifyRequest = VerifyAssetExecute(
            identifier = AssetIdentifier.AssetUuid(args.request.contractConfig.assetUuid),
            assetType = args.request.assetType,
            success = args.request.success,
            message = args.request.message,
            accessRoutes = args.request.contractConfig.accessRoutes,
        )

        return provenanceService.verifyAsset(args.request.provenanceConfig, signer, args.request.contractConfig, verifyRequest)
    }
}
