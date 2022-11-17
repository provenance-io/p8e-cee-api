package io.provenance.api.frameworks.smartcontract

import io.provenance.client.grpc.PbClient

interface SmartContractClient {
    fun createSmartContractClient(
        contractName: String?,
        contractAddress: String?,
        pbClient: PbClient,
    ): Any
}
