package io.provenance.api.domain.usecase.cee.execute

import io.provenance.api.domain.cee.ContractService
import io.provenance.api.domain.provenance.Provenance
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.cee.common.ContractUtilities
import io.provenance.api.domain.usecase.cee.execute.model.ExecuteContractRequestWrapper
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.frameworks.provenance.SingleTx
import io.provenance.api.frameworks.provenance.exceptions.ContractExecutionException
import io.provenance.api.models.cee.execute.ContractExecutionResponse
import io.provenance.api.models.cee.execute.MultipartyContractExecutionResponse
import io.provenance.api.models.cee.execute.SinglePartyContractExecutionResponse
import io.provenance.api.models.p8e.TxResponse
import io.provenance.scope.sdk.FragmentResult
import io.provenance.scope.sdk.SignedResult
import java.util.Base64
import org.springframework.stereotype.Component

@Component
class ExecuteContract(
    private val contractService: ContractService,
    private val provenanceService: Provenance,
    private val getSigner: GetSigner,
    private val contractUtilities: ContractUtilities,
) : AbstractUseCase<ExecuteContractRequestWrapper, ContractExecutionResponse>() {

    override suspend fun execute(args: ExecuteContractRequestWrapper): ContractExecutionResponse {
        val signer = getSigner.execute(GetSignerRequest(args.uuid, args.request.config.account))
        contractUtilities.createClient(args.uuid, args.request.permissions, args.request.participants, args.request.config).use { client ->
            val session = contractUtilities.createSession(args.uuid, client, args.request.permissions, args.request.participants, args.request.config, args.request.records, listOf(args.request.scope)).single()

            return when (val result = contractService.executeContract(client, session)) {
                is SignedResult -> {
                    provenanceService.buildContractTx(args.request.config.provenanceConfig, SingleTx(result)).let {
                        provenanceService.executeTransaction(args.request.config.provenanceConfig, it, signer).let { pbResponse ->
                            SinglePartyContractExecutionResponse(
                                TxResponse(
                                    pbResponse.txhash,
                                    pbResponse.gasWanted.toString(),
                                    pbResponse.gasUsed.toString(),
                                    pbResponse.height.toString(),
                                )
                            )
                        }
                    }
                }
                is FragmentResult -> {
                    client.requestAffiliateExecution(result.envelopeState)
                    MultipartyContractExecutionResponse(
                        Base64.getEncoder().encodeToString(result.envelopeState.toByteArray())
                    )
                }
                else -> throw ContractExecutionException("Contract execution result was not of an expected type.")
            }
        }
    }
}
