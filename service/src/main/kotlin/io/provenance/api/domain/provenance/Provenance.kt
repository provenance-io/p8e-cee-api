package io.provenance.api.domain.provenance

import cosmos.base.abci.v1beta1.Abci
import cosmos.tx.v1beta1.TxOuterClass
import io.provenance.api.frameworks.provenance.ProvenanceTx
import io.provenance.api.models.p8e.ProvenanceConfig
import io.provenance.api.models.p8e.TxBody
import io.provenance.api.models.p8e.TxResponse
import io.provenance.api.models.p8e.contracts.SmartContractConfig
import io.provenance.api.models.p8e.contracts.VOSmartContractConfig
import io.provenance.api.models.p8e.contracts.VOSmartContractLibraryClientCall
import io.provenance.client.grpc.Signer
import io.provenance.metadata.v1.ScopeResponse
import tech.figure.classification.asset.client.domain.execute.OnboardAssetExecute
import tech.figure.classification.asset.client.domain.execute.VerifyAssetExecute
import java.util.UUID

interface Provenance {
    fun onboard(chainId: String, nodeEndpoint: String, signer: Signer, storeTxBody: TxBody): TxResponse
    fun executeTransaction(config: ProvenanceConfig, tx: TxOuterClass.TxBody, signer: Signer): Abci.TxResponse
    fun buildContractTx(config: ProvenanceConfig, tx: ProvenanceTx): TxOuterClass.TxBody
    fun getScope(config: ProvenanceConfig, scopeUuid: UUID, height: Long? = null): ScopeResponse
    fun classifyAsset(config: ProvenanceConfig, signer: Signer, contractConfig: SmartContractConfig, onboardAssetRequest: OnboardAssetExecute<UUID>): TxResponse
    fun verifyAsset(config: ProvenanceConfig, signer: Signer, contractConfig: SmartContractConfig, verifyAssetRequest: VerifyAssetExecute<UUID>): TxResponse
    fun executeValidationOracleTransaction(config: ProvenanceConfig, signer: Signer, contractConfig: VOSmartContractConfig, libraryCall: VOSmartContractLibraryClientCall): TxResponse
    fun queryValidationOracle(config: ProvenanceConfig, contractConfig: VOSmartContractConfig, libraryCall: VOSmartContractLibraryClientCall): String
}
