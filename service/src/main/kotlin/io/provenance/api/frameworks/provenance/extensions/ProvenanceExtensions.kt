package io.provenance.api.frameworks.provenance.extensions

import com.google.protobuf.Any
import cosmos.auth.v1beta1.Auth
import cosmos.base.abci.v1beta1.Abci
import cosmos.base.abci.v1beta1.Abci.TxResponse
import cosmos.base.tendermint.v1beta1.Query
import cosmos.tx.v1beta1.ServiceOuterClass.BroadcastTxResponse
import cosmos.tx.v1beta1.TxOuterClass
import io.provenance.api.frameworks.provenance.BatchTx
import io.provenance.api.frameworks.provenance.SingleTx
import io.provenance.client.grpc.PbClient
import io.provenance.client.protobuf.extensions.getBaseAccount
import io.provenance.scope.contract.proto.Contracts
import java.util.concurrent.TimeUnit

fun BroadcastTxResponse.isError() = txResponse.isError()

fun TxResponse.isError() = code > 0

fun TxResponse.isSuccess() = !isError() && height > 0

fun TxResponse.getError(): String =
    logsList.filter { it.log.isNotBlank() }.takeIf { it.isNotEmpty() }?.joinToString("; ") { it.log } ?: rawLog

fun SingleTx.getErrorResult() =
    this.value.envelopeState.result.contract.considerationsList.firstOrNull { it.result.result == Contracts.ExecutionResult.Result.FAIL }

fun BatchTx.getErrorResult() =
    this.value.mapNotNull { it.envelopeState.result.contract.considerationsList.firstOrNull { it.result.result == Contracts.ExecutionResult.Result.FAIL } }

fun Iterable<Any>.toTxBody(pbClient: PbClient) =
    TxOuterClass.TxBody.newBuilder()
        .setTimeoutHeight(getCurrentHeight(pbClient) + 12L)
        .addAllMessages(this)
        .build()

fun Abci.TxResponse.toTxResponse() = io.provenance.api.models.p8e.TxResponse(this.txhash, this.gasWanted.toString(), this.gasUsed.toString(), this.height.toString())

fun getCurrentHeight(pbClient: PbClient): Long = pbClient.tendermintService
    .withDeadlineAfter(10, TimeUnit.SECONDS)
    .getLatestBlock(Query.GetLatestBlockRequest.getDefaultInstance()).block.header.height

fun getBaseAccount(pbClient: PbClient, address: String): Auth.BaseAccount = pbClient.authClient
    .withDeadlineAfter(10, TimeUnit.SECONDS)
    .getBaseAccount(address)
