package io.provenance.api.domain.cee

import com.google.protobuf.Message
import io.provenance.core.Originator
import io.provenance.metadata.v1.ScopeResponse
import io.provenance.scope.contract.proto.Envelopes
import io.provenance.scope.contract.proto.Specifications
import io.provenance.scope.contract.spec.P8eContract
import io.provenance.scope.sdk.Client
import io.provenance.scope.sdk.ExecutionResult
import io.provenance.scope.sdk.Session
import java.security.PublicKey
import java.util.UUID

interface ContractService {
    fun getContract(contractName: String): Class<out P8eContract>
    fun <T : P8eContract> setupContract(client: Client, contractClass: Class<T>, records: Map<String, Message>, scopeUuid: UUID, sessionUuid: UUID? = null, participants: Map<Specifications.PartyType, Originator>? = null, scope: ScopeResponse? = null, scopeSpecification: String, audiences: Set<PublicKey>): Session
    fun executeContract(client: Client, session: Session): ExecutionResult
    fun executeContract(client: Client, envelope: Envelopes.Envelope): ExecutionResult
}
