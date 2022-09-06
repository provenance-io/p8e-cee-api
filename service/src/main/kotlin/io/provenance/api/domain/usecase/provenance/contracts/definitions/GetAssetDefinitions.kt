package io.provenance.api.domain.usecase.provenance.contracts.definitions

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.provenance.contracts.common.AssetClassficationUtils
import io.provenance.api.domain.usecase.provenance.contracts.definitions.models.GetAssetDefinitionsRequest
import org.springframework.stereotype.Component
import tech.figure.classification.asset.client.domain.model.AssetDefinition

@Component
class GetAssetDefinitions : AbstractUseCase<GetAssetDefinitionsRequest, List<AssetDefinition>>() {
    override suspend fun execute(args: GetAssetDefinitionsRequest) =
        AssetClassficationUtils.runClassificationAction(args.chainId, args.nodeEndpoint, args.contractName) {
            it.queryAssetDefinitions()
        }
}
