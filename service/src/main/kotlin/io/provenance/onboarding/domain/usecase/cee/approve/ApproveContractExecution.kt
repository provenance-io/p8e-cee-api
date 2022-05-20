package io.provenance.onboarding.domain.usecase.cee.approve

import com.google.protobuf.Any
import cosmos.tx.v1beta1.TxOuterClass
import io.provenance.api.models.p8e.TxResponse
import io.provenance.onboarding.domain.provenance.Provenance
import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.cee.approve.models.ApproveContractRequestWrapper
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
) : AbstractUseCase<ApproveContractRequestWrapper, TxResponse>() {
    override suspend fun execute(args: ApproveContractRequestWrapper): TxResponse {
        val client = createClient.execute(CreateClientRequest(args.uuid, args.request.account, args.request.client))
        val envelope = Envelopes.Envelope.newBuilder().mergeFrom(args.request.envelope).build()

        when (val result = client.execute(envelope)) {
            is FragmentResult -> {
               client.approveScopeUpdate(result.envelopeState, args.request.expiration).let {
                    val signer = getSigner.execute(args.uuid)
                    val txBody = TxOuterClass.TxBody.newBuilder().addAllMessages(it.map { msg -> Any.pack(msg, "") }).build()
                    provenance.executeTransaction(args.request.provenanceConfig, txBody, signer).also { broadcast ->

                        client.respondWithApproval(result.envelopeState, broadcast.txhash)
                        return TxResponse(broadcast.txhash, broadcast.gasWanted.toString(), broadcast.gasUsed.toString(), broadcast.height.toString())
                    }
                }
            }
            else -> throw IllegalStateException("Unexpected contract state after approving a fragment. Did not receive fragment result!")
        }
    }
}
