package io.provenance.api.domain.provenance

import com.google.protobuf.Any
import cosmos.base.abci.v1beta1.Abci
import io.provenance.api.frameworks.provenance.ProvenanceTx
import io.provenance.api.models.p8e.ProvenanceConfig
import io.provenance.api.models.p8e.TxResponse
import io.provenance.api.models.p8e.contracts.SmartContractConfig
import io.provenance.client.grpc.Signer
import io.provenance.metadata.v1.ScopeResponse
import java.util.UUID
import tech.figure.classification.asset.client.domain.execute.OnboardAssetExecute
import tech.figure.classification.asset.client.domain.execute.VerifyAssetExecute

interface Provenance {
    fun executeTransaction(config: ProvenanceConfig, messages: Iterable<Any>, signer: Signer): Abci.TxResponse
    fun buildContractTx(tx: ProvenanceTx): Iterable<Any>
    fun getScope(config: ProvenanceConfig, scopeUuid: UUID, height: Long? = null): ScopeResponse
    fun classifyAsset(config: ProvenanceConfig, signer: Signer, contractConfig: SmartContractConfig, onboardAssetRequest: OnboardAssetExecute<UUID>): TxResponse
    fun verifyAsset(config: ProvenanceConfig, signer: Signer, contractConfig: SmartContractConfig, verifyAssetRequest: VerifyAssetExecute<UUID>): TxResponse
}
