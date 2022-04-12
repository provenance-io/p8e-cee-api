package io.provenance.onboarding.frameworks.provenance

import com.google.common.io.BaseEncoding
import com.google.protobuf.Any
import cosmos.tx.v1beta1.ServiceOuterClass
import cosmos.tx.v1beta1.TxOuterClass
import io.provenance.client.grpc.BaseReqSigner
import io.provenance.client.grpc.GasEstimationMethod
import io.provenance.client.grpc.PbClient
import io.provenance.hdwallet.wallet.Account
import io.provenance.onboarding.domain.provenance.Provenance
import io.provenance.onboarding.domain.usecase.common.model.ScopeConfig
import io.provenance.onboarding.domain.usecase.common.model.TxBody
import io.provenance.onboarding.domain.usecase.provenance.tx.model.OnboardAssetResponse
import io.provenance.onboarding.frameworks.provenance.utility.ProvenanceUtils
import org.springframework.stereotype.Component
import java.net.URI
import java.util.UUID

@Component
class ProvenanceService : Provenance {

    override fun onboard(chainId: String, nodeEndpoint: String, account: Account, storeTxBody: TxBody): OnboardAssetResponse {
        val pbClient = setupProvenanceClient(chainId, nodeEndpoint)
        val utility = ProvenanceUtils()

        val txBody = TxOuterClass.TxBody.newBuilder().also {
            storeTxBody.base64.forEach { tx ->
                it.addMessages(Any.parseFrom(BaseEncoding.base64().decode(tx)))
            }
        }.build()

        val response = pbClient.estimateAndBroadcastTx(
            txBody = txBody,
            signers = listOf(BaseReqSigner(utility.getSigner(account))),
            mode = ServiceOuterClass.BroadcastMode.BROADCAST_MODE_SYNC,
            gasAdjustment = 1.5
        ).txResponse

        return OnboardAssetResponse(response.txhash, response.gasWanted.toString(), response.gasUsed.toString(), response.height.toString())
    }

    override fun writeSpecifications(chainId: String, nodeEndpoint: String, account: Account, scopeId: UUID, contractSpecId: UUID, scopeSpecId: UUID, type: String) {
        val pbClient = setupProvenanceClient(chainId, nodeEndpoint)
        val utility = ProvenanceUtils()

        val txBody = if (type == "asset") {
            utility.buildAssetSpecificationMetadataTransaction(
                ScopeConfig(
                    scopeId = scopeId,
                    contractSpecId = contractSpecId,
                    scopeSpecId = scopeSpecId
                ),
                account.address.value
            )
        } else {
            throw IllegalArgumentException("write-specs is misconfigured for type $type")
        }

        pbClient.estimateAndBroadcastTx(
            txBody = txBody,
            signers = listOf(BaseReqSigner(utility.getSigner(account))),
            mode = ServiceOuterClass.BroadcastMode.BROADCAST_MODE_SYNC,
            gasAdjustment = 1.5
        ).also {
            it.txResponse.apply {
                println("TX (height: $height, txhash: $txhash, code: $code, gasWanted: $gasWanted, gasUsed: $gasUsed)")
            }
        }
    }

    private fun setupProvenanceClient(chainId: String, nodeEndpoint: String) = PbClient(chainId, URI(nodeEndpoint), GasEstimationMethod.MSG_FEE_CALCULATION)
}
