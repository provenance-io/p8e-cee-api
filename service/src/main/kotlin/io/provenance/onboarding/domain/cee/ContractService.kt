package io.provenance.onboarding.domain.cee

import com.google.protobuf.Message
import cosmos.base.abci.v1beta1.Abci
import io.provenance.onboarding.frameworks.provenance.SingleTx
import io.provenance.scope.contract.proto.Envelopes
import io.provenance.scope.contract.spec.P8eContract
import io.provenance.scope.sdk.Client
import io.provenance.scope.sdk.ExecutionResult
import io.provenance.scope.sdk.Session
import java.util.UUID

interface ContractService {
    fun getContract(contractName: String): Class<out P8eContract>
    fun <T : P8eContract> setupContract(client: Client, contractClass: Class<T>, records: Map<String, Message>, scopeUuid: UUID, sessionUuid: UUID? = null): Session
    fun executeContract(client: Client, session: Session): ExecutionResult
    fun executeContract(client: Client, envelope: Envelopes.Envelope): ExecutionResult
}
