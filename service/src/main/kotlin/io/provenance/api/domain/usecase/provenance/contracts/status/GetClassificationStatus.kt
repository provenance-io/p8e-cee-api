package io.provenance.api.domain.usecase.provenance.contracts.status

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.provenance.contracts.common.AssetClassficationUtils
import io.provenance.api.domain.usecase.provenance.contracts.status.models.GetStatusOfClassificationRequest
import io.provenance.classification.asset.client.domain.model.AssetScopeAttribute
import org.springframework.stereotype.Component

@Component
class GetClassificationStatus : AbstractUseCase<GetStatusOfClassificationRequest, AssetScopeAttribute>() {
    override suspend fun execute(args: GetStatusOfClassificationRequest) =
        AssetClassficationUtils.runClassificationAction(args.chainId, args.nodeEndpoint, args.contractName) {
            it.queryAssetScopeAttributeByAssetUuid(args.assetUuid)
        }
}
