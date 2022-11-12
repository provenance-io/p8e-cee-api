package io.provenance.api.frameworks.provenance

import com.fasterxml.jackson.databind.ObjectMapper
import cosmos.tx.v1beta1.ServiceOuterClass
import io.provenance.api.domain.smartcontract.SmartContract
import io.provenance.api.frameworks.BaseService
import io.provenance.api.frameworks.provenance.extensions.toTxResponse
import io.provenance.api.models.p8e.ProvenanceConfig
import io.provenance.api.models.p8e.TxResponse
import io.provenance.api.models.p8e.contracts.SmartContractClientLibraryInvocation
import io.provenance.api.models.p8e.contracts.SmartContractConfiguration
import io.provenance.api.util.toPrettyJson
import io.provenance.client.grpc.GasEstimationMethod
import io.provenance.client.grpc.PbClient
import io.provenance.client.grpc.Signer
import io.provenance.scope.encryption.domain.inputstream.DIMEInputStream.Companion.configureProvenance
import org.springframework.stereotype.Component
import tech.figure.classification.asset.client.client.base.ACClient
import tech.figure.classification.asset.client.client.base.ContractIdentifier
import tech.figure.classification.asset.client.client.impl.DefaultACQuerier
import tech.figure.classification.asset.util.objects.ACObjectMapperUtil
import tech.figure.validationoracle.client.client.base.VOClient
import tech.figure.validationoracle.client.client.impl.DefaultVOQuerier
import tech.figure.validationoracle.util.objects.VOObjectMapperUtil
import java.net.URI
import kotlin.reflect.KCallable

/**
 * If a new smart contract is ever added to CEE, it is included in Dependencies.kt and
 *   getSmartContractSpecificClient() needs to be updated to return the proper smart contract client library class based on the className
 *   that is passed in to the smart contract execute transaction and query endpoints.
 */
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
                getSmartContractSpecificClient(libraryInvocation.parameterClassName,
                    contractConfig.contractName,
                    contractConfig.contractAddress,
                    pbClient)
            reflectMethodFromClass(smartContractKotlinClient, libraryInvocation.methodName).call(
                smartContractKotlinClient,
                createClassUsingNameAndPopulateWithJson(libraryInvocation.parameterClassName, libraryInvocation.parameterClassJson),
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
        val smartContractKotlinClient = getSmartContractSpecificClient(libraryCall.parameterClassName,
            contractConfig.contractName,
            contractConfig.contractAddress,
            PbClient(config.chainId, URI(config.nodeEndpoint), GasEstimationMethod.MSG_FEE_CALCULATION)
        )
        return (reflectMethodFromClass(smartContractKotlinClient, libraryCall.methodName).call(
            smartContractKotlinClient,
            createClassUsingNameAndPopulateWithJson(libraryCall.parameterClassName, libraryCall.parameterClassJson)
        ) as Any).toPrettyJson()
    }

    private fun getSmartContractSpecificClient(
        className: String,
        contractName: String?,
        contractAddress: String?,
        pbClient: PbClient,
    ): Any {
        return when {
            className.startsWith("tech.figure.validationoracle.client.domain.execute") -> {
                requireNotNull(contractAddress) { "Your must specify a contract address for this smart contract." }
                VOClient.getDefault(
                    contractIdentifier = tech.figure.validationoracle.client.client.base.ContractIdentifier.Address(
                        contractAddress),
                    pbClient = pbClient,
                    objectMapper = VOObjectMapperUtil.getObjectMapper()
                )
            }
            className.startsWith("tech.figure.validationoracle.client.domain.query") -> {
                requireNotNull(contractAddress) { "Your must specify a contract address for this smart contract." }
                DefaultVOQuerier(
                    contractIdentifier = tech.figure.validationoracle.client.client.base.ContractIdentifier.Address(
                        contractAddress),
                    pbClient = pbClient,
                    objectMapper = ACObjectMapperUtil.getObjectMapper()
                )
            }
            className.startsWith("tech.figure.classification.asset.client.domain.execute") -> {
                ACClient.getDefault(
                    contractIdentifier = when {
                        contractName != null -> ContractIdentifier.Name(contractName)
                        contractAddress != null -> ContractIdentifier.Address(contractAddress)
                        else -> throw IllegalArgumentException("You must specify either a contractName or contractAddress.")
                    },
                    pbClient = pbClient,
                    objectMapper = ACObjectMapperUtil.getObjectMapper(),
                )
            }
            className.startsWith("tech.figure.classification.asset.client.domain.query") -> {
                DefaultACQuerier(
                    contractIdentifier = when {
                        contractName != null -> ContractIdentifier.Name(contractName)
                        contractAddress != null -> ContractIdentifier.Address(contractAddress)
                        else -> throw IllegalArgumentException("You must specify either a contractName or contractAddress.")
                    },
                    pbClient = pbClient,
                    objectMapper = ACObjectMapperUtil.getObjectMapper(),
                )
            }
            else -> throw IllegalStateException("Class $className is an unsupported smart contract client class.")
        }
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
