package io.provenance.api.frameworks.web.external.cee

import io.provenance.api.frameworks.web.Routes
import io.provenance.api.frameworks.web.logging.logExchange
import io.provenance.api.models.cee.approve.ApproveContractBatchRequest
import io.provenance.api.models.cee.approve.ApproveContractRequest
import io.provenance.api.models.cee.execute.ContractExecutionResponse
import io.provenance.api.models.cee.execute.ExecuteContractRequest
import io.provenance.api.models.cee.reject.RejectContractBatchRequest
import io.provenance.api.models.cee.reject.RejectContractRequest
import io.provenance.api.models.cee.submit.SubmitContractBatchExecutionResultRequest
import io.provenance.api.models.cee.submit.SubmitContractExecutionResultRequest
import io.provenance.api.models.p8e.TxResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import java.util.UUID
import mu.KotlinLogging
import org.springdoc.core.annotations.RouterOperation
import org.springdoc.core.annotations.RouterOperations
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.reactive.function.server.coRouter

private val log = KotlinLogging.logger {}

@Configuration
class CeeApi {
    @Bean
    @RouterOperations(
        RouterOperation(
            path = "${Routes.EXTERNAL_BASE_V1}/cee/execute",
            method = arrayOf(RequestMethod.POST),
            produces = ["application/json"],
            operation = Operation(
                tags = ["Contract Execution"],
                operationId = "executeContract",
                method = "POST",
                parameters = [
                    Parameter(
                        name = "x-uuid",
                        required = true,
                        `in` = ParameterIn.HEADER,
                        schema = Schema(implementation = UUID::class),
                    ),
                ],
                requestBody = RequestBody(
                    required = true,
                    content = [Content(schema = Schema(implementation = ExecuteContractRequest::class))]
                ),
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        description = "successful operation",
                        content = [Content(schema = Schema(implementation = ContractExecutionResponse::class))]
                    )
                ]
            )
        ),
        RouterOperation(
            path = "${Routes.EXTERNAL_BASE_V1}/cee/approve",
            method = arrayOf(RequestMethod.POST),
            produces = ["application/json"],
            operation = Operation(
                tags = ["Contract Execution"],
                operationId = "approveContract",
                method = "POST",
                parameters = [
                    Parameter(
                        name = "x-uuid",
                        required = true,
                        `in` = ParameterIn.HEADER,
                        schema = Schema(implementation = UUID::class),
                    ),
                ],
                requestBody = RequestBody(
                    required = true,
                    content = [Content(schema = Schema(implementation = ApproveContractRequest::class))]
                ),
                responses = [
                    ApiResponse(
                        responseCode = "204",
                        description = "successful operation",
                    )
                ]
            )
        ),
        RouterOperation(
            path = "${Routes.EXTERNAL_BASE_V1}/cee/reject",
            method = arrayOf(RequestMethod.POST),
            produces = ["application/json"],
            operation = Operation(
                tags = ["Contract Execution"],
                operationId = "rejectContract",
                method = "POST",
                parameters = [
                    Parameter(
                        name = "x-uuid",
                        required = true,
                        `in` = ParameterIn.HEADER,
                        schema = Schema(implementation = UUID::class),
                    ),
                ],
                requestBody = RequestBody(
                    required = true,
                    content = [Content(schema = Schema(implementation = RejectContractRequest::class))]
                ),
                responses = [
                    ApiResponse(
                        responseCode = "204",
                        description = "successful operation",
                    )
                ]
            )
        ),
        RouterOperation(
            path = "${Routes.EXTERNAL_BASE_V1}/cee/submit",
            method = arrayOf(RequestMethod.POST),
            produces = ["application/json"],
            operation = Operation(
                tags = ["Contract Execution"],
                operationId = "submitContract",
                method = "POST",
                parameters = [
                    Parameter(
                        name = "x-uuid",
                        required = true,
                        `in` = ParameterIn.HEADER,
                        schema = Schema(implementation = UUID::class),
                    ),
                ],
                requestBody = RequestBody(
                    required = true,
                    content = [Content(schema = Schema(implementation = SubmitContractExecutionResultRequest::class))]
                ),
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        description = "successful operation",
                        content = [Content(schema = Schema(implementation = TxResponse::class))]
                    )
                ]
            )
        ),
        RouterOperation(
            path = "${Routes.EXTERNAL_BASE_V1}/cee/batch/submit",
            method = arrayOf(RequestMethod.POST),
            produces = ["application/json"],
            operation = Operation(
                tags = ["Contract Execution"],
                operationId = "submitContractBatch",
                method = "POST",
                parameters = [
                    Parameter(
                        name = "x-uuid",
                        required = true,
                        `in` = ParameterIn.HEADER,
                        schema = Schema(implementation = UUID::class),
                    ),
                ],
                requestBody = RequestBody(
                    required = true,
                    content = [Content(schema = Schema(implementation = SubmitContractBatchExecutionResultRequest::class))]
                ),
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        description = "successful operation",
                        content = [Content(schema = Schema(implementation = TxResponse::class))]
                    )
                ]
            )
        ),
        RouterOperation(
            path = "${Routes.EXTERNAL_BASE_V1}/cee/batch/reject",
            method = arrayOf(RequestMethod.POST),
            produces = ["application/json"],
            operation = Operation(
                tags = ["Contract Execution"],
                operationId = "rejectContractBatch",
                method = "POST",
                parameters = [
                    Parameter(
                        name = "x-uuid",
                        required = true,
                        `in` = ParameterIn.HEADER,
                        schema = Schema(implementation = UUID::class),
                    ),
                ],
                requestBody = RequestBody(
                    required = true,
                    content = [Content(schema = Schema(implementation = RejectContractBatchRequest::class))]
                ),
                responses = [
                    ApiResponse(
                        responseCode = "204",
                        description = "successful operation",
                    )
                ]
            )
        ),
        RouterOperation(
            path = "${Routes.EXTERNAL_BASE_V1}/cee/batch/approve",
            method = arrayOf(RequestMethod.POST),
            produces = ["application/json"],
            operation = Operation(
                tags = ["Contract Execution"],
                operationId = "approveContractBatch",
                method = "POST",
                parameters = [
                    Parameter(
                        name = "x-uuid",
                        required = true,
                        `in` = ParameterIn.HEADER,
                        schema = Schema(implementation = UUID::class),
                    ),
                ],
                requestBody = RequestBody(
                    required = true,
                    content = [Content(schema = Schema(implementation = ApproveContractBatchRequest::class))]
                ),
                responses = [
                    ApiResponse(
                        responseCode = "204",
                        description = "successful operation",
                    )
                ]
            )
        ),
        RouterOperation(
            path = "${Routes.EXTERNAL_BASE_V1}/cee/batch/execute",
            method = arrayOf(RequestMethod.POST),
            produces = ["application/json"],
            operation = Operation(
                tags = ["Contract Execution"],
                operationId = "executeContractBatch",
                method = "POST",
                parameters = [
                    Parameter(
                        name = "x-uuid",
                        required = true,
                        `in` = ParameterIn.HEADER,
                        schema = Schema(implementation = UUID::class),
                    ),
                ],
                requestBody = RequestBody(
                    required = true,
                    content = [Content(schema = Schema(implementation = SubmitContractBatchExecutionResultRequest::class))]
                ),
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        description = "successful operation",
                        content = [Content(schema = Schema(implementation = TxResponse::class))]
                    )
                ]
            )
        ),
    )
    fun externalCeeApiV1(handler: CeeHandler) = coRouter {
        logExchange(log)
        "${Routes.EXTERNAL_BASE_V1}/cee".nest {
            POST("/approve", handler::approveContractExecution)
            POST("/execute", handler::executeContract)
            POST("/submit", handler::submitContractResult)
            POST("/reject", handler::rejectContractExecution)
            GET("/headers", handler::showHeaders)
            "/batch".nest {
                POST("/execute", handler::executeContractBatch)
                POST("/submit", handler::submitContractBatchResult)
                POST("/reject", handler::rejectContractBatchExecution)
                POST("/approve", handler::approveContractBatchExecution)
            }
        }
    }
}
