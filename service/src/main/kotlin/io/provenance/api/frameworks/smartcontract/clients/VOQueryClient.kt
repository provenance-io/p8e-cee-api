package io.provenance.api.frameworks.smartcontract.clients

import io.provenance.api.frameworks.smartcontract.SmartContractClient
import io.provenance.client.grpc.PbClient
import tech.figure.classification.asset.util.objects.ACObjectMapperUtil
import tech.figure.validationoracle.client.client.base.ContractIdentifier
import tech.figure.validationoracle.client.client.impl.DefaultVOQuerier

class VOQueryClient : SmartContractClient {
    override fun createSmartContractClient(
        contractName: String?,
        contractAddress: String?,
        pbClient: PbClient,
    ): Any {
        requireNotNull(contractAddress) { "Your must specify a contract address for this smart contract." }
        return DefaultVOQuerier(
            contractIdentifier = ContractIdentifier.Address(
                contractAddress
            ),
            pbClient = pbClient,
            objectMapper = ACObjectMapperUtil.getObjectMapper()
        )
    }
}
