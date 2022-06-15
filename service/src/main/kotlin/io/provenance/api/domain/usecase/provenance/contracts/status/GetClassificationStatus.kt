package io.provenance.api.domain.usecase.provenance.contracts.status

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.provenance.contracts.status.models.GetStatusOfClassificationRequest
import io.provenance.classification.asset.client.client.base.ACClient
import io.provenance.classification.asset.client.client.base.ContractIdentifier
import io.provenance.classification.asset.client.domain.model.AssetScopeAttribute
import io.provenance.classification.asset.util.objects.ACObjectMapperUtil
import io.provenance.client.grpc.GasEstimationMethod
import io.provenance.client.grpc.PbClient
import java.net.URI
import org.springframework.stereotype.Component

@Component
class GetClassificationStatus : AbstractUseCase<GetStatusOfClassificationRequest, AssetScopeAttribute>() {
    override suspend fun execute(args: GetStatusOfClassificationRequest): AssetScopeAttribute {
        PbClient(args.chainId, URI(args.nodeEndpoint), GasEstimationMethod.MSG_FEE_CALCULATION).use {
            val assetClassificationClient = ACClient.getDefault(
                contractIdentifier = ContractIdentifier.Name(args.contractName),
                pbClient = it,
                objectMapper = ACObjectMapperUtil.getObjectMapper()
            )

            return assetClassificationClient.queryAssetScopeAttributeByAssetUuid(args.assetUuid)
        }
    }
}
