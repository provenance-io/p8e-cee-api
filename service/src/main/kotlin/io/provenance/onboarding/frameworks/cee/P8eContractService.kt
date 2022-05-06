package io.provenance.onboarding.frameworks.cee

import com.google.protobuf.Message
import io.provenance.core.KeyType
import io.provenance.core.Originator
import io.provenance.metadata.v1.ScopeResponse
import io.provenance.onboarding.domain.cee.ContractService
import io.provenance.onboarding.frameworks.provenance.exceptions.ContractExecutionException
import io.provenance.scope.contract.proto.Envelopes.Envelope
import io.provenance.scope.contract.proto.Specifications
import io.provenance.scope.contract.spec.P8eContract
import io.provenance.scope.encryption.util.toJavaPublicKey
import io.provenance.scope.loan.LoanScopeSpecification
import io.provenance.scope.sdk.Client
import io.provenance.scope.sdk.ExecutionResult
import io.provenance.scope.sdk.Session
import java.util.UUID
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class P8eContractService : ContractService {
    private val log = KotlinLogging.logger { }

    override fun getContract(contractName: String): Class<out P8eContract> {
        return Class.forName(contractName).asSubclass(P8eContract::class.java)
    }

    override fun <T : P8eContract> setupContract(
        client: Client,
        contractClass: Class<T>,
        records: Map<String, Message>,
        scopeUuid: UUID,
        sessionUuid: UUID?,
        participants: Map<Specifications.PartyType, Originator>?,
        scope: ScopeResponse?,
    ): Session =
        when (scope) {
            null ->
                client
                    .newSession(contractClass, LoanScopeSpecification::class.java)
                    .setScopeUuid(scopeUuid)
                    .configureSession(records, sessionUuid, participants)
                    .also { session ->
                        log.info("[L: ${session.scopeUuid}, S: ${session.sessionUuid}] ${contractClass.simpleName} has been setup.")
                    }
            else ->
                client
                    .newSession(contractClass, scope)
                    .configureSession(records, sessionUuid, participants)
                    .also { session ->
                        log.info("[L: ${session.scopeUuid}, S: ${session.sessionUuid}] ${contractClass.simpleName} has been setup.")
                    }
        }

    override fun executeContract(client: Client, session: Session): ExecutionResult =
        runCatchingExecutionResult<Session>() {
            client.execute(session)
        }

    override fun executeContract(client: Client, envelope: Envelope): ExecutionResult =
        runCatchingExecutionResult<Envelope>() {
            client.execute(envelope)
        }

    private fun <T> runCatchingExecutionResult(execution: () -> ExecutionResult): ExecutionResult =
        runCatching {
            execution()
        }.fold(
            onSuccess = { result -> result },
            onFailure = { throwable ->
                throw ContractExecutionException("Contract execution failed.", throwable)
            }
        )

    private fun Session.Builder.configureSession(records: Map<String, Message>, sessionUuid: UUID? = null, participants: Map<Specifications.PartyType, Originator>?): Session =
        this.setSessionUuid(sessionUuid ?: UUID.randomUUID())
            .also { records.forEach { record -> it.addProposedRecord(record.key, record.value) } }
            .also {
                participants?.forEach { participant ->
                    it.addParticipant(
                        participant.key,
                        participant.value.keys[KeyType.SIGNING_PUBLIC_KEY].toString().toJavaPublicKey(),
                        participant.value.keys[KeyType.ENCRYPTION_PUBLIC_KEY].toString().toJavaPublicKey()
                    )
                }
            }
            .build()
}
