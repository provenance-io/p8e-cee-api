package io.provenance.onboarding.domain.provenance

import io.provenance.hdwallet.wallet.Account
import io.provenance.onboarding.domain.usecase.common.model.TxBody
import io.provenance.onboarding.domain.usecase.provenance.tx.model.OnboardAssetResponse
import java.util.UUID

interface Provenance {
    fun onboard(chainId: String, nodeEndpoint: String, account: Account, storeTxBody: TxBody): OnboardAssetResponse
    fun writeSpecifications(chainId: String, nodeEndpoint: String, account: Account, scopeId: UUID, contractSpecId: UUID, scopeSpecId: UUID, type: String)
}
