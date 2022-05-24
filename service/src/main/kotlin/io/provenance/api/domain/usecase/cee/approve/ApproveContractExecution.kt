package io.provenance.api.domain.usecase.cee.approve

import com.google.protobuf.Any
import cosmos.tx.v1beta1.TxOuterClass
import io.provenance.api.domain.provenance.Provenance
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.cee.approve.models.ApproveContractRequestWrapper
import io.provenance.api.domain.usecase.cee.common.client.CreateClient
import io.provenance.api.domain.usecase.cee.common.client.model.CreateClientRequest
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.scope.contract.proto.Envelopes
import io.provenance.scope.sdk.FragmentResult
import org.springframework.stereotype.Component

@Component
class ApproveContractExecution(
    private val createClient: CreateClient,
    private val provenance: Provenance,
    private val getSigner: GetSigner,
) : AbstractUseCase<ApproveContractRequestWrapper, Unit>() {
    override suspend fun execute(args: ApproveContractRequestWrapper) {
        val client = createClient.execute(CreateClientRequest(args.uuid, args.request.account, args.request.client))
        val envelope = Envelopes.Envelope.newBuilder().mergeFrom(args.request.envelope).build()

        when (val result = client.execute(envelope)) {
            is FragmentResult -> {
                val approvalTxHash = client.approveScopeUpdate(result.envelopeState, args.request.expiration).let {
                    val signer = getSigner.execute(GetSignerRequest(args.uuid, args.request.account))
                    val txBody = TxOuterClass.TxBody.newBuilder().addAllMessages(it.map { msg -> Any.pack(msg, "") }).build()
                    val broadcast = provenance.executeTransaction(args.request.provenanceConfig, txBody, signer)

                    broadcast.txhash
                }

                client.respondWithApproval(result.envelopeState, approvalTxHash)
            }
        }
    }
}
