package io.provenance.api.domain.usecase.provenance.tx.permissions.fees.create

import com.google.protobuf.Duration
import cosmos.feegrant.v1beta1.Feegrant.AllowedMsgAllowance
import cosmos.feegrant.v1beta1.Feegrant.BasicAllowance
import cosmos.feegrant.v1beta1.Feegrant.PeriodicAllowance
import cosmos.feegrant.v1beta1.Tx.MsgGrantAllowance
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.domain.usecase.provenance.tx.permissions.fees.create.models.GrantFeeGrantRequestWrapper
import io.provenance.api.frameworks.provenance.ProvenanceService
import io.provenance.api.frameworks.provenance.extensions.toAny
import io.provenance.api.frameworks.provenance.extensions.toProtoSpendLimit
import io.provenance.api.frameworks.provenance.extensions.toTxResponse
import io.provenance.api.models.p8e.TxResponse
import io.provenance.api.models.p8e.tx.permissions.fees.Allowance
import io.provenance.api.models.p8e.tx.permissions.fees.Coin
import io.provenance.api.models.p8e.tx.permissions.fees.FeeGrantAllowedMsgAllowance
import io.provenance.api.models.p8e.tx.permissions.fees.FeeGrantBasicAllowance
import io.provenance.api.models.p8e.tx.permissions.fees.FeeGrantPeriodicAllowance
import io.provenance.client.protobuf.extensions.time.toProtoTimestamp
import java.time.OffsetDateTime
import org.springframework.stereotype.Component

@Component
class GrantFeeGrant(
    private val provenanceService: ProvenanceService,
    private val getSigner: GetSigner,
) : AbstractUseCase<GrantFeeGrantRequestWrapper, TxResponse>() {
    override suspend fun execute(args: GrantFeeGrantRequestWrapper): TxResponse {
        val allowance = args.request.grant.allowance
        val signer = getSigner.execute(
            GetSignerRequest(
                args.userID,
                args.request.account
            )
        )

        val allowanceMsg = when (allowance) {
            is FeeGrantBasicAllowance -> buildBasicAllowance(allowance.spendLimit, allowance.expiration)
            is FeeGrantPeriodicAllowance -> buildPeriodicAllowance(
                allowance.periodSpendLimit,
                allowance.feeGrantBasicAllowance?.spendLimit,
                allowance.feeGrantBasicAllowance?.expiration,
                allowance.period
            )
            is FeeGrantAllowedMsgAllowance -> buildAllowedMsgAllowance(allowance.allowedMsgTypes, allowance.allowance)
        }.toAny()

        val message = MsgGrantAllowance.newBuilder()
            .setAllowance(allowanceMsg)
            .setGrantee(args.request.grant.grantee)
            .setGranter(signer.address())
            .build()
            .toAny()

        return provenanceService.executeTransaction(args.request.provenanceConfig, listOf(message), signer).toTxResponse()
    }

    private fun buildBasicAllowance(spendLimit: List<Coin>?, expirationDate: OffsetDateTime?) =
        BasicAllowance.newBuilder().apply {
            spendLimit?.let { coins ->
                addAllSpendLimit(coins.toProtoSpendLimit())
            }

            expirationDate?.let {
                expiration = it.toProtoTimestamp()
            }
        }.build()

    private fun buildPeriodicAllowance(
        periodSpendLimit: List<Coin>,
        totalSpendLimit: List<Coin>?,
        expirationDate: OffsetDateTime?,
        duration: String,
    ) =
        PeriodicAllowance.newBuilder().apply {
            basic = buildBasicAllowance(totalSpendLimit, expirationDate)
            period = Duration.newBuilder().setSeconds(duration.toLong()).build()
            addAllPeriodSpendLimit(periodSpendLimit.toProtoSpendLimit())
        }.build()

    private fun buildAllowedMsgAllowance(allowedMsgTypes: List<String>, feeGrantAllowance: Allowance) =
        AllowedMsgAllowance.newBuilder().apply {
            allowance = buildAllowance(feeGrantAllowance).toAny()
            addAllAllowedMessages(allowedMsgTypes)
        }.build()

    private fun buildAllowance(
        allowance: Allowance,
    ) =
        when (allowance) {
            is FeeGrantBasicAllowance -> buildBasicAllowance(allowance.spendLimit, allowance.expiration)
            is FeeGrantPeriodicAllowance -> buildPeriodicAllowance(
                allowance.periodSpendLimit,
                allowance.feeGrantBasicAllowance?.spendLimit,
                allowance.feeGrantBasicAllowance?.expiration,
                allowance.period
            )
            else -> throw IllegalArgumentException("Inner allowance must not be of type AllowedMsgAllowance!")
        }
}
