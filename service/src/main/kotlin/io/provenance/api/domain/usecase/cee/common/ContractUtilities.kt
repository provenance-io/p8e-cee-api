package io.provenance.api.domain.usecase.cee.common

import com.google.protobuf.Message
import io.provenance.api.domain.cee.ContractParser
import io.provenance.api.domain.cee.ContractService
import io.provenance.api.domain.provenance.Provenance
import io.provenance.api.domain.usecase.cee.common.client.CreateClient
import io.provenance.api.domain.usecase.cee.common.client.model.CreateClientRequest
import io.provenance.api.domain.usecase.common.originator.EntityManager
import io.provenance.api.domain.usecase.common.originator.models.KeyManagementConfigWrapper
import io.provenance.api.models.account.Participant
import io.provenance.api.models.cee.ParserConfig
import io.provenance.api.models.cee.execute.ExecuteContractConfig
import io.provenance.api.models.cee.execute.ScopeInfo
import io.provenance.api.models.p8e.PermissionInfo
import io.provenance.client.protobuf.extensions.isSet
import io.provenance.metadata.v1.ScopeResponse
import io.provenance.scope.contract.annotations.Input
import io.provenance.scope.contract.spec.P8eContract
import io.provenance.scope.encryption.util.toJavaPublicKey
import io.provenance.scope.sdk.Client
import io.provenance.scope.sdk.Session
import java.util.UUID
import kotlin.reflect.KType
import kotlin.reflect.full.functions
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class ContractUtilities(
    private val contractService: ContractService,
    private val provenanceService: Provenance,
    private val contractParser: ContractParser,
    private val createClient: CreateClient,
    private val entityManager: EntityManager
) {
    private val log = KotlinLogging.logger { }

    suspend fun createClient(uuid: UUID, permissions: PermissionInfo?, participants: List<Participant>, config: ExecuteContractConfig): Client {
        val audiences = entityManager.hydrateKeys(permissions, participants)
        return createClient.execute(CreateClientRequest(uuid, config.account, config.client, audiences))
    }

    suspend fun createSession(uuid: UUID, permissions: PermissionInfo?, participants: List<Participant>, config: ExecuteContractConfig, records: Map<String, Any>, scopes: List<ScopeInfo>): List<Session> {
        val audiences = entityManager.hydrateKeys(permissions, participants)
        val client = createClient.execute(CreateClientRequest(uuid, config.account, config.client, audiences))
        val contract = contractService.getContract(config.contract.contractName)
        val parsedRecords = getRecords(contractParser, records, contract, config.contract.parserConfig)

        val participantsMap = participants.associate {
            it.partyType to entityManager.getEntity(KeyManagementConfigWrapper(it.uuid, config.account.keyManagementConfig))
        }

        return scopes.map {
            val scope = provenanceService.getScope(config.provenanceConfig, it.scopeUuid)
            val scopeToUse: ScopeResponse? = if (scope.scope.scope.isSet() && !scope.scope.scope.scopeId.isEmpty) scope else null
            contractService.setupContract(
                client,
                contract,
                parsedRecords,
                it.scopeUuid,
                it.sessionUuid,
                participantsMap,
                scopeToUse,
                config.contract.scopeSpecificationName,
                audiences.map { it.encryptionKey.toJavaPublicKey() }.toSet()
            )
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun getRecords(contractParser: ContractParser, records: Map<String, Any>, contract: Class<out P8eContract>, parserConfig: ParserConfig?): Map<String, Message> {
        val contractRecords = mutableMapOf<String, Message>()

        try {
            contract.kotlin.functions.forEach { func ->
                func.parameters.forEach { param ->
                    (param.annotations.firstOrNull { it is Input } as? Input)?.let { input ->
                        val parameterClass = Class.forName(param.type.toClassNameString())
                        records.getOrDefault(input.name, null)?.let {

                            val record = when (val parser = parserConfig?.name?.let { name -> contractParser.getParser(name) }) {
                                null -> {
                                    contractParser.parseInput(it, parameterClass)
                                }
                                else -> {
                                    parser.parse(it, parameterClass, parserConfig.descriptors)
                                }
                            }

                            contractRecords[input.name] = record
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            log.error("Failed to get inputs for contract ${contract.simpleName}")
            throw ex
        }

        return contractRecords
    }

    private fun KType?.toClassNameString(): String? = this?.classifier?.toString()?.drop("class ".length)
}
