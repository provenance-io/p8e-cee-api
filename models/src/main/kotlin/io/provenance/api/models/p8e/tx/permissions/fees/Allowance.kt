package io.provenance.api.models.p8e.tx.permissions.fees

import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.OffsetDateTime

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@class")
sealed class Allowance

data class FeeGrantPeriodicAllowance(
    val feeGrantBasicAllowance: FeeGrantBasicAllowance?,
    val periodSpendLimit: List<Coin>,
    val period: String
) : Allowance()

data class FeeGrantBasicAllowance(
    val spendLimit: List<Coin>?,
    val expiration: OffsetDateTime?,
) : Allowance()

data class FeeGrantAllowedMsgAllowance(
    val allowance: Allowance,
    val allowedMsgTypes: List<String>
) : Allowance()
