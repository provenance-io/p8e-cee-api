package io.provenance.api.domain.provenance

import cosmos.base.abci.v1beta1.Abci
import cosmos.tx.v1beta1.TxOuterClass
import io.provenance.api.models.p8e.ProvenanceConfig
import io.provenance.api.models.p8e.TxResponse
import io.provenance.client.grpc.Signer
import io.provenance.metadata.v1.ScopeResponse
import io.provenance.api.models.p8e.TxBody
import io.provenance.api.frameworks.provenance.ProvenanceTx
import java.util.UUID

interface Provenance {
    fun onboard(chainId: String, nodeEndpoint: String, signer: Signer, storeTxBody: TxBody): TxResponse
    fun executeTransaction(config: ProvenanceConfig, tx: TxOuterClass.TxBody, signer: Signer): Abci.TxResponse
    fun buildContractTx(config: ProvenanceConfig, tx: ProvenanceTx): TxOuterClass.TxBody?
    fun getScope(config: ProvenanceConfig, scopeUuid: UUID): ScopeResponse
}
