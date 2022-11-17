package io.provenance.api.frameworks.smartcontract.clients

import io.provenance.api.frameworks.smartcontract.SmartContractClient
import io.provenance.client.grpc.PbClient
import tech.figure.classification.asset.client.client.base.ACClient
import tech.figure.classification.asset.client.client.base.ContractIdentifier
import tech.figure.classification.asset.util.objects.ACObjectMapperUtil

class ACExecuteClient : SmartContractClient {
    override fun createSmartContractClient(
        contractName: String?,
        contractAddress: String?,
        pbClient: PbClient,
    ): Any {
        return ACClient.getDefault(
            contractIdentifier = when {
                contractName != null -> ContractIdentifier.Name(contractName)
                contractAddress != null -> ContractIdentifier.Address(contractAddress)
                else -> throw IllegalArgumentException("You must specify either a contractName or contractAddress.")
            },
            pbClient = pbClient,
            objectMapper = ACObjectMapperUtil.getObjectMapper(),
        )
    }
}
