package io.provenance.onboarding.domain.provenance

import cosmos.base.abci.v1beta1.Abci
import io.provenance.client.grpc.Signer
import io.provenance.hdwallet.wallet.Account
import io.provenance.onboarding.domain.usecase.common.model.TxBody
import io.provenance.onboarding.domain.usecase.provenance.tx.model.OnboardAssetResponse
import io.provenance.onboarding.frameworks.provenance.ProvenanceTx
import io.provenance.scope.sdk.Session

interface Provenance {
    fun onboard(chainId: String, nodeEndpoint: String, account: Account, storeTxBody: TxBody): OnboardAssetResponse
    fun executeTransaction(chainId: String, nodeEndpoint: String, session: Session, tx: ProvenanceTx, signer: Signer): Abci.TxResponse
}
