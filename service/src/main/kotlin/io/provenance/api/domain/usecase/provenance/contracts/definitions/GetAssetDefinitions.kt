package io.provenance.api.domain.usecase.provenance.contracts.definitions

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.provenance.contracts.common.AssetClassficationUtils
import io.provenance.api.domain.usecase.provenance.contracts.definitions.models.GetAssetDefinitionsRequest
import io.provenance.classification.asset.client.domain.model.QueryAssetDefinitionsResponse
import org.springframework.stereotype.Component

@Component
class GetAssetDefinitions : AbstractUseCase<GetAssetDefinitionsRequest, QueryAssetDefinitionsResponse>() {
    override suspend fun execute(args: GetAssetDefinitionsRequest) =
        AssetClassficationUtils.runClassificationAction(args.chainId, args.nodeEndpoint, args.contractName) {
            it.queryAssetDefinitions()
        }
}
