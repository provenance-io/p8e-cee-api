package io.provenance.api.frameworks.smartcontract

import com.fasterxml.jackson.databind.ObjectMapper
import cosmos.tx.v1beta1.ServiceOuterClass
import io.provenance.api.domain.smartcontract.SmartContract
import io.provenance.api.frameworks.BaseService
import io.provenance.api.frameworks.provenance.extensions.toTxResponse
import io.provenance.api.frameworks.smartcontract.clients.ACExecuteClient
import io.provenance.api.frameworks.smartcontract.clients.ACQueryClient
import io.provenance.api.frameworks.smartcontract.clients.VOExecuteClient
import io.provenance.api.frameworks.smartcontract.clients.VOQueryClient
import io.provenance.api.models.p8e.ProvenanceConfig
import io.provenance.api.models.p8e.TxResponse
import io.provenance.api.models.p8e.contracts.SmartContractClientLibraryInvocation
import io.provenance.api.models.p8e.contracts.SmartContractConfiguration
import io.provenance.api.util.toPrettyJson
import io.provenance.client.grpc.GasEstimationMethod
import io.provenance.client.grpc.PbClient
import io.provenance.client.grpc.Signer
import io.provenance.scope.encryption.domain.inputstream.DIMEInputStream.Companion.configureProvenance
import io.provenance.scope.objectstore.util.orThrow
import org.springframework.stereotype.Component
import java.net.URI
import kotlin.reflect.KCallable

/**
 * If you want to support a new smart contract in CEE
 * 1. Include in Dependencies.kt
 * 2. Add a Kotlin client class implementing SmartContractClient in io.provenance.api.frameworks.smartcontract.clients
 * 3. Update smartContractSupportedClients below to associate the "startsWith" path of class names to use for the Kotlin class
 *       you added in step 2.
 *
 *  We could replace this map with something reflective to make these contracts truly closed, like Spring beans.
 */
val smartContractSupportedClients: Map<String, SmartContractClient> = mapOf(
    "tech.figure.validationoracle.client.domain.execute" to VOExecuteClient(),
    "tech.figure.validationoracle.client.domain.query" to VOQueryClient(),
    "tech.figure.classification.asset.client.domain.execute" to ACExecuteClient(),
    "tech.figure.classification.asset.client.domain.query" to ACQueryClient(),
)

@Component
class SmartContractService : SmartContract, BaseService() {

    override fun executeSmartContractTransaction(
        config: ProvenanceConfig,
        signer: Signer,
        contractConfig: SmartContractConfiguration,
        libraryInvocation: SmartContractClientLibraryInvocation,
    ): TxResponse =
        tryAction(config, signer) { pbClient, account, offset ->
            val smartContractKotlinClient =
                getSmartContractSpecificClient(
                    libraryInvocation.parameterClassName,
                    contractConfig.contractName,
                    contractConfig.contractAddress,
                    pbClient
                )
            reflectMethodFromClass(smartContractKotlinClient, libraryInvocation.methodName).call(
                smartContractKotlinClient,
                createClassUsingNameAndPopulateWithJson(
                    libraryInvocation.parameterClassName,
                    libraryInvocation.parameterClassJson
                ),
                signer,
                tech.figure.validationoracle.client.client.base.BroadcastOptions(
                    broadcastMode = ServiceOuterClass.BroadcastMode.BROADCAST_MODE_BLOCK,
                    sequenceOffset = offset,
                    baseAccount = account
                )
            ) as ServiceOuterClass.BroadcastTxResponse
        }.txResponse.toTxResponse()

    override fun querySmartContract(
        config: ProvenanceConfig,
        contractConfig: SmartContractConfiguration,
        libraryCall: SmartContractClientLibraryInvocation,
    ): String {
        val smartContractKotlinClient = getSmartContractSpecificClient(
            libraryCall.parameterClassName,
            contractConfig.contractName,
            contractConfig.contractAddress,
            PbClient(config.chainId, URI(config.nodeEndpoint), GasEstimationMethod.MSG_FEE_CALCULATION)
        )
        return (
            reflectMethodFromClass(smartContractKotlinClient, libraryCall.methodName).call(
                smartContractKotlinClient,
                createClassUsingNameAndPopulateWithJson(libraryCall.parameterClassName, libraryCall.parameterClassJson)
            ) as Any
            ).toPrettyJson()
    }

    @Suppress("ThrowsCount")
    private fun getSmartContractSpecificClient(
        className: String,
        contractName: String?,
        contractAddress: String?,
        pbClient: PbClient,
    ): Any {

        val clientClassName: String = smartContractSupportedClients.keys.filter { cn -> className.startsWith(cn) }
            .first()
            .orThrow { throw IllegalStateException("Class $className is an unsupported smart contract client class.") }
        return smartContractSupportedClients.get(clientClassName)!!.createSmartContractClient(
            contractName,
            contractAddress,
            pbClient
        )
    }

    private fun reflectMethodFromClass(classInstance: Any, methodName: String): KCallable<*> {
        val methods: Collection<KCallable<*>> = classInstance.javaClass.kotlin.members
        return methods.find { it.name == methodName }
            ?: throw java.lang.IllegalArgumentException("Method name $methodName was not found in ${classInstance.javaClass.name}")
    }

    private fun createClassUsingNameAndPopulateWithJson(className: String, json: String): Any {
        val clazz = Class.forName(className) // This throws a ClassNotFoundException
        return ObjectMapper().configureProvenance().readValue(json, clazz) // This throws a JsonMappingException
    }
}
