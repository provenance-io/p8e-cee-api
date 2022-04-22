package io.provenance.onboarding.domain.usecase.cee

import io.provenance.core.KeyType
import io.provenance.onboarding.domain.cee.ContractService
import io.provenance.onboarding.domain.provenance.Provenance
import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.cee.model.ExecuteContractRequest
import io.provenance.onboarding.domain.usecase.common.originator.GetOriginator
import io.provenance.onboarding.domain.usecase.provenance.account.GetAccount
import io.provenance.onboarding.frameworks.provenance.utility.ProvenanceUtils
import io.provenance.scope.contract.proto.Specifications
import io.provenance.scope.encryption.model.DirectKeyRef
import io.provenance.scope.encryption.util.toJavaPrivateKey
import io.provenance.scope.encryption.util.toJavaPublicKey
import io.provenance.scope.sdk.Affiliate
import io.provenance.scope.sdk.Client
import io.provenance.scope.sdk.ClientConfig
import io.provenance.scope.sdk.SharedClient
import java.net.URI
import java.security.KeyPair
import java.util.concurrent.TimeUnit
import org.springframework.stereotype.Component

@Component
class ExecuteContract(
    private val getOriginator: GetOriginator,
    private val contractService: ContractService,
    private val provenanceService: Provenance,
    private val getAccount: GetAccount
) : AbstractUseCase<ExecuteContractRequest, Unit>() {
    override suspend fun execute(args: ExecuteContractRequest) {
        val utils = ProvenanceUtils()
        val sharedClient = SharedClient(
            ClientConfig(
                mainNet = !args.isTestNet,
                cacheJarSizeInBytes = 4L * 1024 * 1024, // ~ 4 MB,
                cacheRecordSizeInBytes = 0L,
                cacheSpecSizeInBytes = 0L,
                disableContractLogs = !args.isTestNet,
                osConcurrencySize = 6,
                osGrpcUrl = URI(args.objectStoreUrl),
                osChannelCustomizeFn = { channelBuilder ->
                    channelBuilder
                        .idleTimeout(1, TimeUnit.MINUTES)
                        .keepAliveTime(10, TimeUnit.SECONDS)
                        .keepAliveTimeout(10, TimeUnit.SECONDS)
                }
            )
        )

        val originator = getOriginator.execute(args.account.originatorUuid)
        val affiliate = Affiliate(
            signingKeyRef = DirectKeyRef(KeyPair(originator.keys[KeyType.SIGNING_PUBLIC_KEY].toString().toJavaPublicKey(), originator.keys[KeyType.SIGNING_PRIVATE_KEY].toString().toJavaPrivateKey())),
            encryptionKeyRef = DirectKeyRef(KeyPair(originator.keys[KeyType.SIGNING_PUBLIC_KEY].toString().toJavaPublicKey(), originator.keys[KeyType.SIGNING_PRIVATE_KEY].toString().toJavaPrivateKey())),
            Specifications.PartyType.valueOf(args.partyType),
        )

        val client = Client(sharedClient, affiliate)
        val account = getAccount.execute(args.account)

        val contract = contractService.getContract(args.contractName)
        val session = contractService.setupContract(client, contract, emptyMap(), args.scopeUuid, args.sessionUuid)
        val signer = utils.getSigner(account)

        contractService.executeContract(client, signer, contract, session) { tx ->
            provenanceService.executeTransaction(args.chainId, args.nodeEndpoint, session, tx, signer)
        }
    }
}
