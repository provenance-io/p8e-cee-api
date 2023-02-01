package io.provenance.api.domain.usecase.provenance.tx.permissions.fees.get

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.common.errors.NotFoundError
import io.provenance.api.domain.usecase.provenance.tx.permissions.fees.get.models.GetFeeGrantAllowanceRequestWrapper
import io.provenance.api.frameworks.provenance.extensions.toModel
import io.provenance.api.models.p8e.tx.permissions.fees.FeeGrant
import io.provenance.client.grpc.GasEstimationMethod
import io.provenance.client.grpc.PbClient
import io.provenance.client.protobuf.extensions.getFeeGrant
import java.net.URI
import org.springframework.stereotype.Component

@Component
class GetFeeGrant : AbstractUseCase<GetFeeGrantAllowanceRequestWrapper, FeeGrant>() {
    override suspend fun execute(args: GetFeeGrantAllowanceRequestWrapper) =
        PbClient(args.request.chainId, URI(args.request.nodeEndpoint), GasEstimationMethod.MSG_FEE_CALCULATION).use { pbClient ->
            runCatching {
                pbClient.feegrantClient.getFeeGrant(args.request.granter, args.request.grantee)
            }
                .fold(
                    onSuccess = { it.toModel() },
                    onFailure = { error ->
                        if (error.message?.contains("fee-grant not found") == true) {
                            throw NotFoundError("fee-grant not found for granter: ${args.request.granter} and grantee: ${args.request.grantee}")
                        } else throw error
                    }
                )
        }
}
