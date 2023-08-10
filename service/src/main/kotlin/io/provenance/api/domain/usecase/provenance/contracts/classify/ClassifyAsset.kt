package io.provenance.api.domain.usecase.provenance.contracts.classify

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.domain.usecase.provenance.contracts.classify.models.ClassifyAssetRequestWrapper
import io.provenance.api.frameworks.provenance.ProvenanceService
import io.provenance.api.models.p8e.TxResponse
import org.springframework.stereotype.Component
import tech.figure.classification.asset.client.domain.execute.OnboardAssetExecute
import tech.figure.classification.asset.client.domain.model.AssetIdentifier

@Component
class ClassifyAsset(
    private val provenanceService: ProvenanceService,
    private val getSigner: GetSigner
) : AbstractUseCase<ClassifyAssetRequestWrapper, TxResponse>() {
    override suspend fun execute(args: ClassifyAssetRequestWrapper): TxResponse {
        val signer = getSigner.execute(
            GetSignerRequest(
                args.Entity,
                args.request.account,
            )
        )

        val assetRequest = OnboardAssetExecute(
            identifier = AssetIdentifier.AssetUuid(args.request.contractConfig.assetUuid),
            assetType = args.request.contractConfig.assetType,
            verifierAddress = args.request.contractConfig.verifierAddress,
            accessRoutes = args.request.contractConfig.accessRoutes,
        )

        return provenanceService.classifyAsset(args.request.provenanceConfig, signer, args.request.contractConfig, assetRequest)
    }
}
