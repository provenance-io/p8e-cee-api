package io.provenance.api.frameworks.web.external.cee

import io.provenance.api.domain.usecase.cee.approve.ApproveContractBatchExecution
import io.provenance.api.domain.usecase.cee.approve.ApproveContractExecution
import io.provenance.api.domain.usecase.cee.approve.models.ApproveContractBatchRequestWrapper
import io.provenance.api.domain.usecase.cee.approve.models.ApproveContractRequestWrapper
import io.provenance.api.domain.usecase.cee.execute.ExecuteContract
import io.provenance.api.domain.usecase.cee.execute.ExecuteContractBatch
import io.provenance.api.domain.usecase.cee.execute.model.ExecuteContractBatchRequestWrapper
import io.provenance.api.domain.usecase.cee.execute.model.ExecuteContractRequestWrapper
import io.provenance.api.domain.usecase.cee.reject.RejectContractBatchExecution
import io.provenance.api.domain.usecase.cee.reject.RejectContractExecution
import io.provenance.api.domain.usecase.cee.reject.models.RejectContractBatchRequestWrapper
import io.provenance.api.domain.usecase.cee.reject.models.RejectContractExecutionRequestWrapper
import io.provenance.api.domain.usecase.cee.submit.SubmitContractBatchExecutionResult
import io.provenance.api.domain.usecase.cee.submit.SubmitContractExecutionResult
import io.provenance.api.domain.usecase.cee.submit.models.SubmitContractBatchExecutionResultRequestWrapper
import io.provenance.api.domain.usecase.cee.submit.models.SubmitContractExecutionResultRequestWrapper
import io.provenance.api.frameworks.web.misc.foldToServerResponse
import io.provenance.api.frameworks.web.misc.getEntity
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody

private val log = KotlinLogging.logger("CeeHandler")

@Component
class CeeHandler(
    private val executeContract: ExecuteContract,
    private val approveContractExecution: ApproveContractExecution,
    private val rejectContractExecution: RejectContractExecution,
    private val submitContract: SubmitContractExecutionResult,
    private val executeContractBatch: ExecuteContractBatch,
    private val submitExecuteContractBatch: SubmitContractBatchExecutionResult,
    private val approveContractBatchExecution: ApproveContractBatchExecution,
    private val rejectContractBatchExecution: RejectContractBatchExecution,
) {
    suspend fun executeContract(req: ServerRequest): ServerResponse = runCatching {
        executeContract.execute(ExecuteContractRequestWrapper(req.getEntity(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun submitContractResult(req: ServerRequest): ServerResponse = runCatching {
        submitContract.execute(SubmitContractExecutionResultRequestWrapper(req.getEntity(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun approveContractExecution(req: ServerRequest): ServerResponse = runCatching {
        approveContractExecution.execute(ApproveContractRequestWrapper(req.getEntity(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun rejectContractExecution(req: ServerRequest): ServerResponse = runCatching {
        rejectContractExecution.execute(RejectContractExecutionRequestWrapper(req.getEntity(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun executeContractBatch(req: ServerRequest): ServerResponse = runCatching {
        executeContractBatch.execute(ExecuteContractBatchRequestWrapper(req.getEntity(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun submitContractBatchResult(req: ServerRequest): ServerResponse = runCatching {
        submitExecuteContractBatch.execute(SubmitContractBatchExecutionResultRequestWrapper(req.getEntity(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun approveContractBatchExecution(req: ServerRequest): ServerResponse = runCatching {
        approveContractBatchExecution.execute(ApproveContractBatchRequestWrapper(req.getEntity(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun rejectContractBatchExecution(req: ServerRequest): ServerResponse = runCatching {
        rejectContractBatchExecution.execute(RejectContractBatchRequestWrapper(req.getEntity(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun showHeaders(req: ServerRequest): ServerResponse = runCatching {
        req.headers().also { log.info { "headers: $it" } }
    }.foldToServerResponse()
}
