package com.figure.onboarding.domain.provenance

import com.figure.onboarding.domain.usecase.common.model.TxBody
import com.figure.onboarding.domain.usecase.provenance.tx.model.OnboardAssetResponse
import io.provenance.hdwallet.wallet.Account
import java.util.UUID

interface Provenance {
    fun onboard(chainId: String, nodeEndpoint: String, account: Account, storeTxBody: TxBody): OnboardAssetResponse
    fun writeSpecifications(chainId: String, nodeEndpoint: String, account: Account, scopeId: UUID, contractSpecId: UUID, scopeSpecId: UUID, type: String)
}
