package io.provenance.onboarding.domain.usecase.provenance.account

import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.common.originator.GetOriginator
import io.provenance.onboarding.domain.usecase.common.model.AccountInfo
import io.provenance.onboarding.frameworks.provenance.utility.ProvenanceUtils
import io.provenance.core.KeyType
import io.provenance.hdwallet.wallet.Account
import java.lang.IllegalStateException
import org.springframework.stereotype.Component

@Component
class GetAccount(
    private val getOriginator: GetOriginator
) : AbstractUseCase<AccountInfo, Account>() {
    override suspend fun execute(args: AccountInfo): Account {
        val utils = ProvenanceUtils()

        val originator = getOriginator.execute(args.originatorUuid)

        return originator.keys[KeyType.MNEMONIC]?.let { mnemonic ->
            utils.getAccount(mnemonic.toString(), args.isTestNet, args.keyRingIndex, args.keyIndex)
        } ?: throw IllegalStateException("Account does not exist: ${args.originatorUuid}.")
    }
}
