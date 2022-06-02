package io.provenance.api.domain.usecase.provenance.contracts.verify

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.domain.usecase.provenance.contracts.verify.models.VerifyAssetRequestWrapper
import io.provenance.api.frameworks.provenance.ProvenanceService
import io.provenance.api.models.p8e.TxResponse
import io.provenance.classification.asset.client.domain.execute.VerifyAssetBody
import io.provenance.classification.asset.client.domain.execute.VerifyAssetExecute
import io.provenance.classification.asset.client.domain.model.AssetIdentifier
import org.springframework.stereotype.Component

@Component
class VerifyAsset(
    private val provenanceService: ProvenanceService,
    private val getSigner: GetSigner
) : AbstractUseCase<VerifyAssetRequestWrapper, TxResponse>() {
    override suspend fun execute(args: VerifyAssetRequestWrapper): TxResponse {
        val signer = getSigner.execute(
            GetSignerRequest(
                args.uuid,
                args.request.account,
            )
        )

        val verifyRequest = VerifyAssetExecute(
            VerifyAssetBody(
                identifier = AssetIdentifier.AssetUuid(args.request.contractConfig.assetUuid),
                success = args.request.success,
                message = args.request.message,
                accessRoutes = args.request.contractConfig.accessRoutes,
            )
        )

        return provenanceService.verifyAsset(args.request.provenanceConfig, signer, args.request.contractConfig, verifyRequest)
    }
}
