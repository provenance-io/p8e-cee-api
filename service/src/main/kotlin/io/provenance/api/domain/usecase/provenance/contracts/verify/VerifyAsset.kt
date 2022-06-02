package io.provenance.api.domain.usecase.provenance.contracts.verify

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.domain.usecase.provenance.contracts.verify.models.VerifyAssetRequestWrapper
import io.provenance.api.frameworks.provenance.ProvenanceService
import io.provenance.api.models.p8e.TxResponse
import io.provenance.classification.asset.client.domain.execute.VerifyAssetBody
import io.provenance.classification.asset.client.domain.execute.VerifyAssetExecute
import io.provenance.classification.asset.client.domain.model.AssetIdentifier
import io.provenance.hdwallet.bip39.MnemonicWords
import io.provenance.hdwallet.ec.extensions.toJavaECPublicKey
import io.provenance.hdwallet.wallet.Account
import io.provenance.hdwallet.wallet.Wallet
import io.provenance.scope.objectstore.util.toHex
import org.springframework.stereotype.Component

@Component
class VerifyAsset(
    private val provenanceService: ProvenanceService,
    private val getSigner: GetSigner
) : AbstractUseCase<VerifyAssetRequestWrapper, TxResponse>() {
    override suspend fun execute(args: VerifyAssetRequestWrapper): TxResponse {
        val signer = getSigner.execute(
            GetSignerRequest(
                args.uuid,
                args.request.account,
            )
        )

        val verifyRequest = VerifyAssetExecute(
            VerifyAssetBody(
                identifier = AssetIdentifier.AssetUuid(args.request.contractConfig.assetUuid),
                success = args.request.success,
                message = args.request.message,
                accessRoutes = args.request.contractConfig.accessRoutes,
            )
        )

        val testnet = true
        val keyRingIndex = 0
        val keyIndex = 0
        val wallet = Wallet.fromMnemonic("", "", MnemonicWords.of(""))
        val accountPath = when (testnet) {
            true -> "m/44'/1'/0'/$keyRingIndex/$keyIndex'"
            false -> "m/505'/1'/0'/$keyRingIndex/$keyIndex"
        }
        val account: Account = wallet[accountPath]

        println(account.keyPair.publicKey.toJavaECPublicKey().toHex())

        return provenanceService.verifyAsset(args.request.provenanceConfig, signer, args.request.contractConfig, verifyRequest)
    }
}
