@file:Suppress("TooManyFunctions")
package io.provenance.api.frameworks.provenance.extensions

import com.google.protobuf.Any
import com.google.protobuf.Message
import com.google.protobuf.Timestamp
import cosmos.auth.v1beta1.Auth
import cosmos.base.abci.v1beta1.Abci
import cosmos.base.abci.v1beta1.Abci.TxResponse
import cosmos.base.tendermint.v1beta1.Query
import cosmos.base.v1beta1.CoinOuterClass
import cosmos.feegrant.v1beta1.Feegrant
import cosmos.tx.v1beta1.ServiceOuterClass.BroadcastTxResponse
import cosmos.tx.v1beta1.TxOuterClass
import io.provenance.api.frameworks.provenance.BatchTx
import io.provenance.api.frameworks.provenance.SingleTx
import io.provenance.api.models.p8e.AudienceKeyPair
import io.provenance.api.models.p8e.tx.permissions.fees.Allowance
import io.provenance.api.models.p8e.tx.permissions.fees.Coin
import io.provenance.api.models.p8e.tx.permissions.fees.FeeGrant
import io.provenance.api.models.p8e.tx.permissions.fees.FeeGrantAllowedMsgAllowance
import io.provenance.api.models.p8e.tx.permissions.fees.FeeGrantBasicAllowance
import io.provenance.api.models.p8e.tx.permissions.fees.FeeGrantPeriodicAllowance
import io.provenance.client.grpc.PbClient
import io.provenance.client.protobuf.extensions.getBaseAccount
import io.provenance.scope.contract.proto.Contracts
import io.provenance.scope.encryption.util.getAddress
import io.provenance.scope.encryption.util.toJavaPublicKey
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

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

fun Any.toTxBody(pbClient: PbClient) =
    TxOuterClass.TxBody.newBuilder()
        .setTimeoutHeight(getCurrentHeight(pbClient) + 12L)
        .addMessages(this)
        .build()

fun Abci.TxResponse.toTxResponse() = io.provenance.api.models.p8e.TxResponse(this.txhash, this.gasWanted.toString(), this.gasUsed.toString(), this.height.toString())

fun getCurrentHeight(pbClient: PbClient): Long = pbClient.tendermintService
    .withDeadlineAfter(10, TimeUnit.SECONDS)
    .getLatestBlock(Query.GetLatestBlockRequest.getDefaultInstance()).block.header.height

fun getBaseAccount(pbClient: PbClient, address: String): Auth.BaseAccount = pbClient.authClient
    .withDeadlineAfter(10, TimeUnit.SECONDS)
    .getBaseAccount(address)

fun Set<AudienceKeyPair>.toMessageSet(isMainnet: Boolean): Set<String> = map {
    it.signingKey.toJavaPublicKey().getAddress(isMainnet)
}.toSet()

fun Message.toAny(typeUrlPrefix: String = ""): Any = Any.pack(this, typeUrlPrefix)

fun Iterable<Message>.toAny(typeUrlPrefix: String = ""): List<Any> = this.map { msg -> Any.pack(msg, typeUrlPrefix) }

fun Feegrant.Grant.toModel() = FeeGrant(granter, grantee, allowance.toFeegrantAllowance())

fun Any.toFeegrantAllowance(): Allowance? =
    when {
        this.typeUrl.endsWith("v1beta1.BasicAllowance") ->
            this.unpack(Feegrant.BasicAllowance::class.java)
                .let {
                    FeeGrantBasicAllowance(it.spendLimitList.toCoinList(), it.expiration.toDateTime())
                }
        this.typeUrl.endsWith("v1beta1.PeriodicAllowance") ->
            this.unpack(Feegrant.PeriodicAllowance::class.java)
                .let {
                    FeeGrantPeriodicAllowance(
                        it.basic.toDto(),
                        it.periodSpendLimitList.toCoinList(),
                        it.period.seconds.toString(),
                    )
                }
        this.typeUrl.endsWith("v1beta1.AllowedMsgAllowance") ->
            this.unpack(Feegrant.AllowedMsgAllowance::class.java)
                .let {
                    FeeGrantAllowedMsgAllowance(it.allowance.toFeegrantAllowance()!!, it.allowedMessagesList)
                }
        else -> null.also { logger.error("Invalid feegrant type: ${this.typeUrl}") }
    }

fun List<CoinOuterClass.Coin>.toCoinList() = this.map { Coin(it.amount, it.denom) }
fun Feegrant.BasicAllowance.toDto() = FeeGrantBasicAllowance(this.spendLimitList.toCoinList(), this.expiration.toDateTime())
fun Timestamp.toDateTime() = OffsetDateTime.ofInstant(java.time.Instant.ofEpochSecond(this.seconds, this.nanos.toLong()), ZoneId.systemDefault())
fun List<Coin>.toProtoSpendLimit() = map { CoinOuterClass.Coin.newBuilder().setAmount(it.amount).setDenom(it.denom).build() }
