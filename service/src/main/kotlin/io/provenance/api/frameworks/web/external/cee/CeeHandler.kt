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
import io.provenance.api.frameworks.web.misc.getEntityID
import io.provenance.api.frameworks.web.misc.respond
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody

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
        executeContract.execute(ExecuteContractRequestWrapper(req.getEntityID(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun submitContractResult(req: ServerRequest): ServerResponse = runCatching {
        submitContract.execute(SubmitContractExecutionResultRequestWrapper(req.getEntityID(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun approveContractExecution(req: ServerRequest): ServerResponse = runCatching {
        approveContractExecution.execute(ApproveContractRequestWrapper(req.getEntityID(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun rejectContractExecution(req: ServerRequest): ServerResponse = runCatching {
        rejectContractExecution.execute(RejectContractExecutionRequestWrapper(req.getEntityID(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun executeContractBatch(req: ServerRequest): ServerResponse = runCatching {
        executeContractBatch.execute(ExecuteContractBatchRequestWrapper(req.getEntityID(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun submitContractBatchResult(req: ServerRequest): ServerResponse = runCatching {
        submitExecuteContractBatch.execute(SubmitContractBatchExecutionResultRequestWrapper(req.getEntityID(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun approveContractBatchExecution(req: ServerRequest): ServerResponse = runCatching {
        approveContractBatchExecution.execute(ApproveContractBatchRequestWrapper(req.getEntityID(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun rejectContractBatchExecution(req: ServerRequest): ServerResponse = runCatching {
        rejectContractBatchExecution.execute(RejectContractBatchRequestWrapper(req.getEntityID(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun showHeaders(req: ServerRequest): ServerResponse = respond {
        data class DebugHeaders(
            val address: String?,
            val granterAddress: String?,
            val uuid: String?
        )

        DebugHeaders(
            address = req.headers().firstHeader("x-figure-tech-address"),
            granterAddress = req.headers().firstHeader("x-figure-tech-granter-address"),
            uuid = req.headers().firstHeader("x-uuid"),
        )
    }
}
