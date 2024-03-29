package io.provenance.api.frameworks.cee

import com.google.protobuf.Message
import io.provenance.api.domain.cee.ContractService
import io.provenance.api.frameworks.provenance.exceptions.ContractExecutionException
import io.provenance.entity.KeyEntity
import io.provenance.entity.KeyType
import io.provenance.metadata.v1.ScopeResponse
import io.provenance.scope.contract.proto.Envelopes.Envelope
import io.provenance.scope.contract.proto.Specifications
import io.provenance.scope.contract.spec.P8eContract
import io.provenance.scope.contract.spec.P8eScopeSpecification
import io.provenance.scope.sdk.Client
import io.provenance.scope.sdk.ExecutionResult
import io.provenance.scope.sdk.Session
import java.security.PublicKey
import java.util.UUID
import kotlin.reflect.full.isSubclassOf
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class P8eContractService : ContractService {
    private val log = KotlinLogging.logger("P8eContractService")

    override fun getContract(contractName: String): Class<out P8eContract> {
        return Class.forName(contractName).asSubclass(P8eContract::class.java)
    }

    override fun <T : P8eContract> setupContract(
        client: Client,
        contractClass: Class<T>,
        records: Map<String, Message>,
        scopeUuid: UUID,
        sessionUuid: UUID?,
        participants: Map<Specifications.PartyType, KeyEntity>?,
        scope: ScopeResponse?,
        scopeSpecification: String,
        audiences: Set<PublicKey>
    ): Session =
        when (scope) {
            null -> {
                val scopeSpec = Class.forName(scopeSpecification).let { specification ->
                    specification.kotlin.isSubclassOf(P8eScopeSpecification::class).takeIf { it }?.let {
                        specification.asSubclass(P8eScopeSpecification::class.java)
                    } ?: throw IllegalArgumentException("Defined scope specification name was not a subclass of P8eScopeSpecification.")
                }

                client
                    .newSession(contractClass, scopeSpec)
                    .setScopeUuid(scopeUuid)
                    .configureSession(records, sessionUuid, participants?.filter { it.key != client.affiliate.partyType }, audiences)
                    .also { session ->
                        log.info("[L: ${session.scopeUuid}, S: ${session.sessionUuid}] ${contractClass.simpleName} has been setup.")
                    }
            }
            else ->
                client
                    .newSession(contractClass, scope)
                    .configureSession(records, sessionUuid, participants?.filter { it.key != client.affiliate.partyType }, audiences)
                    .also { session ->
                        log.info("[L: ${session.scopeUuid}, S: ${session.sessionUuid}] ${contractClass.simpleName} has been setup.")
                    }
        }

    override fun executeContract(client: Client, session: Session): ExecutionResult =
        runCatchingExecutionResult {
            client.execute(session)
        }

    override fun executeContract(client: Client, envelope: Envelope): ExecutionResult =
        runCatchingExecutionResult {
            client.execute(envelope)
        }

    private fun runCatchingExecutionResult(execution: () -> ExecutionResult): ExecutionResult = runCatching {
        execution()
    }.getOrElse { throwable ->
        throw ContractExecutionException("Contract execution failed: ${throwable.message}", throwable)
    }

    private fun Session.Builder.configureSession(
        records: Map<String, Message>,
        sessionUuid: UUID? = null,
        participants: Map<Specifications.PartyType, KeyEntity>?,
        audiences: Set<PublicKey>,
    ): Session =
        this.setSessionUuid(sessionUuid ?: UUID.randomUUID())
            .also {
                records.forEach { record -> it.addProposedRecord(record.key, record.value) }

                participants?.forEach { participant ->
                    it.addParticipant(
                        participant.key,
                        participant.value.publicKey(KeyType.SIGNING),
                        participant.value.publicKey(KeyType.ENCRYPTION),
                    )
                }
            }
            .also { it.addDataAccessKeys(audiences) }
            .build()
}
