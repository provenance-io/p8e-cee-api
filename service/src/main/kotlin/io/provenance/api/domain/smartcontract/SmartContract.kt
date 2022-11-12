package io.provenance.api.domain.smartcontract

import io.provenance.api.models.p8e.ProvenanceConfig
import io.provenance.api.models.p8e.TxResponse
import io.provenance.api.models.p8e.contracts.SmartContractClientLibraryInvocation
import io.provenance.api.models.p8e.contracts.SmartContractConfiguration
import io.provenance.client.grpc.Signer

interface SmartContract {
    fun executeSmartContractTransaction(config: ProvenanceConfig, signer: Signer, contractConfig: SmartContractConfiguration, libraryInvocation: SmartContractClientLibraryInvocation): TxResponse
    fun querySmartContract(config: ProvenanceConfig, contractConfig: SmartContractConfiguration, libraryCall: SmartContractClientLibraryInvocation): String
}
