package io.provenance.onboarding.domain.usecase.cee.approve

import com.google.protobuf.Any
import cosmos.tx.v1beta1.TxOuterClass
import io.provenance.api.models.cee.ApproveContractRequest
import io.provenance.onboarding.domain.provenance.Provenance
import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.cee.common.client.CreateClient
import io.provenance.onboarding.domain.usecase.cee.common.client.model.CreateClientRequest
import io.provenance.onboarding.domain.usecase.provenance.account.GetSigner
import io.provenance.scope.contract.proto.Envelopes
import io.provenance.scope.sdk.FragmentResult
import org.springframework.stereotype.Component

@Component
class ApproveContractExecution(
    private val createClient: CreateClient,
    private val provenance: Provenance,
    private val getSigner: GetSigner,
) : AbstractUseCase<ApproveContractRequest, Unit>() {
    override suspend fun execute(args: ApproveContractRequest) {
        val client = createClient.execute(CreateClientRequest(args.account, args.client))
        val envelope = Envelopes.Envelope.newBuilder().mergeFrom(args.envelope).build()

        when (val result = client.execute(envelope)) {
            is FragmentResult -> {
                val approvalTxHash = client.approveScopeUpdate(result.envelopeState, args.expiration).let {
                    val signer = getSigner.execute(args.account)
                    val txBody = TxOuterClass.TxBody.newBuilder().addAllMessages(it.map { msg -> Any.pack(msg, "") }).build()
                    val broadcast = provenance.executeTransaction(args.provenanceConfig, txBody, signer)

                    broadcast.txhash
                }

                client.respondWithApproval(result.envelopeState, approvalTxHash)
            }
        }
    }
}
