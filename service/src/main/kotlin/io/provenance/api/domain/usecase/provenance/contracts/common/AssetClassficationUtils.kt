package io.provenance.api.domain.usecase.provenance.contracts.common

import io.provenance.classification.asset.client.client.base.ACClient
import io.provenance.classification.asset.client.client.base.ContractIdentifier
import io.provenance.classification.asset.util.objects.ACObjectMapperUtil
import io.provenance.client.grpc.GasEstimationMethod
import io.provenance.client.grpc.PbClient
import java.net.URI

class AssetClassficationUtils private constructor() {
    companion object {
        fun <T> runClassificationAction(chainId: String, node: String, contract: String, action: (ACClient) -> T): T {
            PbClient(chainId, URI(node), GasEstimationMethod.MSG_FEE_CALCULATION).use {
                val assetClassificationClient = ACClient.getDefault(
                    contractIdentifier = ContractIdentifier.Name(contract),
                    pbClient = it,
                    objectMapper = ACObjectMapperUtil.getObjectMapper()
                )

                return action(assetClassificationClient)
            }
        }
    }
}
