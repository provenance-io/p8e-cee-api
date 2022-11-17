package io.provenance.api.frameworks.smartcontract.clients

import io.provenance.api.frameworks.smartcontract.SmartContractClient
import io.provenance.client.grpc.PbClient
import tech.figure.classification.asset.client.client.base.ContractIdentifier
import tech.figure.classification.asset.client.client.impl.DefaultACQuerier
import tech.figure.classification.asset.util.objects.ACObjectMapperUtil

class ACQueryClient : SmartContractClient {
    override fun createSmartContractClient(
        contractName: String?,
        contractAddress: String?,
        pbClient: PbClient,
    ): Any {
        return DefaultACQuerier(
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
