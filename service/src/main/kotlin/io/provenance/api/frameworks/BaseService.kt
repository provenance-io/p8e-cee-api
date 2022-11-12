package io.provenance.api.frameworks

import cosmos.auth.v1beta1.Auth
import cosmos.tx.v1beta1.ServiceOuterClass
import io.provenance.api.frameworks.provenance.CachedAccountSequence
import io.provenance.api.frameworks.provenance.extensions.getBaseAccount
import io.provenance.api.models.p8e.ProvenanceConfig
import io.provenance.client.grpc.GasEstimationMethod
import io.provenance.client.grpc.PbClient
import io.provenance.client.grpc.Signer
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

/**
 * Abstract base to share common functionality across services.
 */
open class BaseService {
    private val cachedSequenceMap = ConcurrentHashMap<String, CachedAccountSequence>()

    fun tryAction(
        config: ProvenanceConfig,
        signer: Signer,
        action: (pbClient: PbClient, account: Auth.BaseAccount, offset: Int) -> ServiceOuterClass.BroadcastTxResponse,
    ): ServiceOuterClass.BroadcastTxResponse {
        PbClient(config.chainId, URI(config.nodeEndpoint), GasEstimationMethod.MSG_FEE_CALCULATION).use { pbClient ->
            val account = getBaseAccount(pbClient, signer.address())
            val cachedOffset = cachedSequenceMap.getOrPut(signer.address()) { CachedAccountSequence() }

            runCatching {
                action(pbClient, account, cachedOffset.getAndIncrementOffset(account.sequence))
            }.fold(
                onSuccess = {
                    return it
                },
                onFailure = {
                    cachedOffset.getAndDecrement(account.sequence)
                    throw it
                }
            )
        }
    }
}
