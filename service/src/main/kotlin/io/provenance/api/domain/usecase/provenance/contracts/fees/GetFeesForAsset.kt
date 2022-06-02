package io.provenance.api.domain.usecase.provenance.contracts.fees

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.provenance.contracts.fees.models.GetFeesForAssetRequest
import io.provenance.classification.asset.client.client.base.ACClient
import io.provenance.classification.asset.client.client.base.ContractIdentifier
import io.provenance.classification.asset.client.domain.model.AssetDefinition
import io.provenance.classification.asset.util.objects.ACObjectMapperUtil
import io.provenance.client.grpc.GasEstimationMethod
import io.provenance.client.grpc.PbClient
import java.net.URI
import org.springframework.stereotype.Component

@Component
class GetFeesForAsset : AbstractUseCase<GetFeesForAssetRequest, AssetDefinition>() {
    override suspend fun execute(args: GetFeesForAssetRequest): AssetDefinition {
        PbClient(args.chainId, URI(args.nodeEndpoint), GasEstimationMethod.MSG_FEE_CALCULATION).let {
            val assetClassificationClient = ACClient.getDefault(
                contractIdentifier = ContractIdentifier.Name(args.contractName),
                pbClient = it,
                objectMapper = ACObjectMapperUtil.getObjectMapper()
            )

            return assetClassificationClient.queryAssetDefinitionByAssetType(args.assetType)
        }
    }
}
