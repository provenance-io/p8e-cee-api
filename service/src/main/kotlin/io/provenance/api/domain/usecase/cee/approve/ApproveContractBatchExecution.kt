package io.provenance.api.domain.usecase.cee.approve

import com.google.protobuf.Any
import cosmos.authz.v1beta1.Tx
import cosmos.tx.v1beta1.TxOuterClass
import io.provenance.api.domain.provenance.Provenance
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.cee.approve.models.ApproveContractBatchRequestWrapper
import io.provenance.api.domain.usecase.cee.common.client.CreateClient
import io.provenance.api.domain.usecase.cee.common.client.model.CreateClientRequest
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.frameworks.provenance.extensions.toTxResponse
import io.provenance.api.models.cee.approve.ApproveContractExecutionBatchResponse
import io.provenance.api.models.cee.approve.ApproveContractExecutionErrorResponse
import io.provenance.api.models.cee.approve.ApproveContractExecutionResponse
import io.provenance.api.util.toPrettyJson
import io.provenance.scope.contract.proto.Envelopes
import io.provenance.scope.sdk.FragmentResult
import java.util.Base64
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class ApproveContractBatchExecution(
    private val createClient: CreateClient,
    private val provenance: Provenance,
    private val getSigner: GetSigner,
) : AbstractUseCase<ApproveContractBatchRequestWrapper, ApproveContractExecutionBatchResponse>() {
    private val log = KotlinLogging.logger { }

    override suspend fun execute(args: ApproveContractBatchRequestWrapper): ApproveContractExecutionBatchResponse {
        val responses = mutableListOf<ApproveContractExecutionResponse>()
        val errors = mutableListOf<ApproveContractExecutionErrorResponse>()
        val executionResults = mutableListOf<Pair<Envelopes.EnvelopeState, List<Tx.MsgGrant>>>()
        val signer = getSigner.execute(GetSignerRequest(args.uuid, args.request.account))
        createClient.execute(CreateClientRequest(args.uuid, args.request.account, args.request.client)).use { client ->

            args.request.approvals.forEach {
                val envelope = Envelopes.Envelope.newBuilder().mergeFrom(it.envelope).build()
                val result = client.execute(envelope)

                if (result is FragmentResult) {
                    client.approveScopeUpdate(result.envelopeState, it.expiration).let { grant ->
                        executionResults.add(Pair(result.envelopeState, grant))
                    }
                }
            }

            val chunked = executionResults.chunked(args.request.chunkSize)
            chunked.forEachIndexed { index, it ->
                runCatching {
                    val messages = it.flatMap { grantList -> grantList.second.map { grant -> Any.pack(grant, "") } }
                    val txBody = TxOuterClass.TxBody.newBuilder().addAllMessages(messages).build()
                    val broadcast = provenance.executeTransaction(args.request.provenanceConfig, txBody, signer)

                    it.forEach { executions ->
                        client.respondWithApproval(executions.first, broadcast.txhash)
                        responses.add(ApproveContractExecutionResponse(Base64.getEncoder().encodeToString(executions.first.toByteArray()), broadcast.toTxResponse()))
                    }
                }.fold(
                    onSuccess = {
                        log.info("Successfully processed batch $index of ${chunked.size}")
                    },
                    onFailure = {
                        errors.add(
                            ApproveContractExecutionErrorResponse(
                                "Tx Execution Exception",
                                it.toPrettyJson()
                            )
                        )
                    }
                )
            }
        }

        return responses

        return ApproveContractExecutionBatchResponse(
            responses,
            errors
        )
    }
}
