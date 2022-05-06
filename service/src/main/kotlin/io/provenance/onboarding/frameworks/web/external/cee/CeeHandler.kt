package io.provenance.onboarding.frameworks.web.external.cee

import io.provenance.onboarding.domain.usecase.cee.approve.ApproveContractExecution
import io.provenance.onboarding.domain.usecase.cee.execute.ExecuteContract
import io.provenance.onboarding.domain.usecase.cee.reject.RejectContractExecution
import io.provenance.onboarding.domain.usecase.cee.submit.SubmitContractExecutionResult
import io.provenance.onboarding.frameworks.web.misc.foldToServerResponse
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody

@Component
class CeeHandler(
    private val executeContract: ExecuteContract,
    private val approveContractExecution: ApproveContractExecution,
    private val rejectContractExecution: RejectContractExecution,
    private val submitContract: SubmitContractExecutionResult
) {
    suspend fun executeContract(req: ServerRequest): ServerResponse = runCatching {
        executeContract.execute(req.awaitBody())
    }.foldToServerResponse()

    suspend fun submitContractResult(req: ServerRequest): ServerResponse = runCatching {
        submitContract.execute(req.awaitBody())
    }.foldToServerResponse()

    suspend fun approveContractExecution(req: ServerRequest): ServerResponse = runCatching {
        approveContractExecution.execute(req.awaitBody())
    }.foldToServerResponse()

    suspend fun rejectContractExecution(req: ServerRequest): ServerResponse = runCatching {
        rejectContractExecution.execute(req.awaitBody())
    }.foldToServerResponse()
}
