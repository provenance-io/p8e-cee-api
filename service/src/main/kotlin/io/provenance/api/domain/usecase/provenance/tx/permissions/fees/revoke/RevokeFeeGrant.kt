package io.provenance.api.domain.usecase.provenance.tx.permissions.fees.revoke

import cosmos.feegrant.v1beta1.Tx.MsgRevokeAllowance
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.domain.usecase.provenance.tx.permissions.fees.revoke.models.RevokeFeeGrantRequestWrapper
import io.provenance.api.frameworks.provenance.ProvenanceService
import io.provenance.api.frameworks.provenance.extensions.toAny
import io.provenance.api.frameworks.provenance.extensions.toTxResponse
import io.provenance.api.models.p8e.TxResponse
import org.springframework.stereotype.Component

@Component
class RevokeFeeGrant(
    private val provenanceService: ProvenanceService,
    private val getSigner: GetSigner
) : AbstractUseCase<RevokeFeeGrantRequestWrapper, TxResponse>() {
    override suspend fun execute(args: RevokeFeeGrantRequestWrapper): TxResponse {
        val signer = getSigner.execute(
            GetSignerRequest(
                args.entity,
                args.request.account
            )
        )

        val message = MsgRevokeAllowance.newBuilder()
            .setGrantee(args.request.grantee)
            .setGranter(signer.address())
            .build()
            .toAny()

        return provenanceService.executeTransaction(args.request.provenanceConfig, listOf(message), signer).toTxResponse()
    }
}
