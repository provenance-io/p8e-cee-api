package io.provenance.onboarding.domain.usecase.provenance.account

import io.provenance.api.models.account.AccountInfo
import io.provenance.client.grpc.Signer
import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.common.originator.GetOriginator
import io.provenance.onboarding.frameworks.provenance.utility.ProvenanceUtils
import org.springframework.stereotype.Component
import java.security.PrivateKey
import java.security.PublicKey

@Component
class GetSigner(
    private val getOriginator: GetOriginator
) : AbstractUseCase<AccountInfo, Signer>() {
    override suspend fun execute(args: AccountInfo): Signer {
        val utils = ProvenanceUtils()

        val originator = getOriginator.execute(args.originatorUuid)

        return originator.signingPublicKey().let { public ->
            originator.signingPrivateKey().let { private ->
                utils.getSigner(public as PublicKey, private as PrivateKey, !args.isTestNet)
            }
        }
    }
}
