package io.provenance.onboarding.frameworks.provenance.extensions

import cosmos.auth.v1beta1.Auth
import cosmos.base.abci.v1beta1.Abci.TxResponse
import cosmos.base.tendermint.v1beta1.Query
import cosmos.tx.v1beta1.ServiceOuterClass.BroadcastTxResponse
import io.provenance.client.grpc.PbClient
import io.provenance.onboarding.frameworks.provenance.SingleTx
import io.provenance.scope.contract.proto.Contracts
import java.util.concurrent.TimeUnit
import io.provenance.client.protobuf.extensions.getBaseAccount

fun BroadcastTxResponse.isError() = txResponse.isError()

fun TxResponse.isError() = code > 0

fun TxResponse.isSuccess() = !isError() && height > 0

fun TxResponse.getError(): String =
    logsList.filter { it.log.isNotBlank() }.takeIf { it.isNotEmpty() }?.joinToString("; ") { it.log } ?: rawLog

fun SingleTx.getErrorResult() =
    this.value.envelopeState.result.contract.considerationsList.firstOrNull { it.result.result == Contracts.ExecutionResult.Result.FAIL }

fun getCurrentHeight(pbClient: PbClient): Long = pbClient.tendermintService
    .withDeadlineAfter(10, TimeUnit.SECONDS)
    .getLatestBlock(Query.GetLatestBlockRequest.getDefaultInstance()).block.header.height

fun getBaseAccount(pbClient: PbClient, address: String): Auth.BaseAccount = pbClient.authClient
    .withDeadlineAfter(10, TimeUnit.SECONDS)
    .getBaseAccount(address)
