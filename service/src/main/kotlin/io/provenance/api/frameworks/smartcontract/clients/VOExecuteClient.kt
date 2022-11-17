package io.provenance.api.frameworks.smartcontract.clients

import io.provenance.api.frameworks.smartcontract.SmartContractClient
import io.provenance.client.grpc.PbClient
import tech.figure.validationoracle.client.client.base.VOClient
import tech.figure.validationoracle.util.objects.VOObjectMapperUtil

class VOExecuteClient : SmartContractClient {
    override fun createSmartContractClient(
        contractName: String?,
        contractAddress: String?,
        pbClient: PbClient,
    ): Any {
        requireNotNull(contractAddress) { "Your must specify a contract address for this smart contract." }
        return VOClient.getDefault(
            contractIdentifier = tech.figure.validationoracle.client.client.base.ContractIdentifier.Address(
                contractAddress
            ),
            pbClient = pbClient,
            objectMapper = VOObjectMapperUtil.getObjectMapper()
        )
    }
}
