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
import io.provenance.api.frameworks.provenance.exceptions.ContractExecutionException
import io.provenance.api.frameworks.provenance.extensions.toTxResponse
import io.provenance.api.models.p8e.TxResponse
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
        val envelope = Envelopes.Envelope.newBuilder().mergeFrom(args.request.approval.envelope).build()
        createClient.execute(CreateClientRequest(args.uuid, args.request.account, args.request.client)).use { client ->

            val result = client.execute(envelope)
            if (result is FragmentResult) {
                val tx = client.approveScopeUpdate(result.envelopeState, args.request.approval.expiration).let {
                    val signer = getSigner.execute(GetSignerRequest(args.uuid, args.request.account))
                    val txBody = TxOuterClass.TxBody.newBuilder().addAllMessages(it.map { msg -> Any.pack(msg, "") }).build()
                    provenance.executeTransaction(args.request.provenanceConfig, txBody, signer)
                }

                client.respondWithApproval(result.envelopeState, tx.txhash)
                return tx.toTxResponse()
            } else throw ContractExecutionException("Attempted to approve an envelope that did not result in a fragment. Only non-approved envelopes should be sent!")
        }
    }
}
