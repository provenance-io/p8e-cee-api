package io.provenance.api.domain.usecase.provenance.contracts.fees

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.provenance.contracts.common.AssetClassficationUtils
import io.provenance.api.domain.usecase.provenance.contracts.fees.models.GetFeesForAssetRequest
import io.provenance.classification.asset.client.domain.model.AssetDefinition
import org.springframework.stereotype.Component

@Component
class GetFeesForAsset : AbstractUseCase<GetFeesForAssetRequest, AssetDefinition>() {
    override suspend fun execute(args: GetFeesForAssetRequest): AssetDefinition =
        AssetClassficationUtils.runClassificationAction(args.chainId, args.nodeEndpoint, args.contractName) {
            it.queryAssetDefinitionByAssetType(args.assetType)
        }
}
