package io.provenance.api.frameworks.web.external.provenance

import io.provenance.api.frameworks.web.Routes
import io.provenance.api.frameworks.web.logging.logExchange
import io.provenance.api.models.p8e.TxBody
import io.provenance.api.models.p8e.TxResponse
import io.provenance.api.models.p8e.contracts.ClassifyAssetRequest
import io.provenance.api.models.p8e.contracts.VerifyAssetRequest
import io.provenance.api.models.p8e.tx.CreateTxRequest
import io.provenance.api.models.p8e.tx.ExecuteTxRequest
import io.provenance.api.models.p8e.tx.permissions.UpdateScopeDataAccessRequest
import io.provenance.classification.asset.client.domain.model.AssetDefinition
import io.provenance.metadata.v1.ScopeResponse
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
class ProvenanceApi {
    @Bean
    @RouterOperations(
        RouterOperation(
            path = "${Routes.EXTERNAL_BASE_V1}/p8e/tx/generate",
            method = arrayOf(RequestMethod.POST),
            produces = ["application/json"],
            operation = Operation(
                tags = ["Provenance"],
                operationId = "generate",
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
                    content = [Content(schema = Schema(implementation = CreateTxRequest::class))]
                ),
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        description = "successful operation",
                        content = [Content(schema = Schema(implementation = TxBody::class))]
                    )
                ]
            )
        ),
        RouterOperation(
            path = "${Routes.EXTERNAL_BASE_V1}/p8e/tx/execute",
            method = arrayOf(RequestMethod.POST),
            produces = ["application/json"],
            operation = Operation(
                tags = ["Provenance"],
                operationId = "execute",
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
                    content = [Content(schema = Schema(implementation = ExecuteTxRequest::class))]
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
            path = "${Routes.EXTERNAL_BASE_V1}/p8e/scope/query",
            method = arrayOf(RequestMethod.GET),
            produces = ["application/json"],
            operation = Operation(
                tags = ["Provenance"],
                operationId = "getScopeQuery",
                method = "POST",
                parameters = [
                    Parameter(
                        name = "x-uuid",
                        required = true,
                        `in` = ParameterIn.HEADER,
                        schema = Schema(implementation = UUID::class),
                    ),
                    Parameter(
                        name = "scopeUuid",
                        required = true,
                        `in` = ParameterIn.QUERY,
                        schema = Schema(implementation = UUID::class)
                    ),
                    Parameter(
                        name = "chainId",
                        required = true,
                        `in` = ParameterIn.QUERY,
                        schema = Schema(implementation = String::class)
                    ),
                    Parameter(
                        name = "nodeEndpoint",
                        required = true,
                        `in` = ParameterIn.QUERY,
                        schema = Schema(implementation = String::class)
                    ),
                    Parameter(
                        name = "objectStoreUrl",
                        required = true,
                        `in` = ParameterIn.QUERY,
                        schema = Schema(implementation = String::class)
                    ),
                    Parameter(
                        name = "height",
                        required = true,
                        `in` = ParameterIn.QUERY,
                        schema = Schema(implementation = Long::class)
                    ),
                    Parameter(
                        name = "hydate",
                        required = true,
                        `in` = ParameterIn.QUERY,
                        schema = Schema(implementation = Boolean::class)
                    ),
                ],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        description = "successful operation",
                        content = [Content(schema = Schema(implementation = ScopeResponse::class))]
                    )
                ]
            )
        ),
        RouterOperation(
            path = "${Routes.EXTERNAL_BASE_V1}/p8e/classify",
            method = arrayOf(RequestMethod.POST),
            produces = ["application/json"],
            operation = Operation(
                tags = ["Provenance"],
                operationId = "classifyAsset",
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
                    content = [Content(schema = Schema(implementation = ClassifyAssetRequest::class))]
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
            path = "${Routes.EXTERNAL_BASE_V1}/p8e/verify",
            method = arrayOf(RequestMethod.POST),
            produces = ["application/json"],
            operation = Operation(
                tags = ["Provenance"],
                operationId = "verifyAsset",
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
                    content = [Content(schema = Schema(implementation = VerifyAssetRequest::class))]
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
            path = "${Routes.EXTERNAL_BASE_V1}/p8e/fees",
            method = arrayOf(RequestMethod.GET),
            produces = ["application/json"],
            operation = Operation(
                tags = ["Provenance"],
                operationId = "getFees",
                method = "POST",
                parameters = [
                    Parameter(
                        name = "x-uuid",
                        required = true,
                        `in` = ParameterIn.HEADER,
                        schema = Schema(implementation = UUID::class),
                    ),
                    Parameter(
                        name = "contractName",
                        required = true,
                        `in` = ParameterIn.QUERY,
                        schema = Schema(implementation = String::class),
                    ),
                    Parameter(
                        name = "assetType",
                        required = true,
                        `in` = ParameterIn.QUERY,
                        schema = Schema(implementation = String::class),
                    ),
                    Parameter(
                        name = "chainId",
                        required = true,
                        `in` = ParameterIn.QUERY,
                        schema = Schema(implementation = String::class),
                    ),
                    Parameter(
                        name = "nodeEndpoint",
                        required = true,
                        `in` = ParameterIn.QUERY,
                        schema = Schema(implementation = String::class),
                    ),
                ],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        description = "successful operation",
                        content = [Content(schema = Schema(implementation = AssetDefinition::class))]
                    )
                ]
            )
        ),
        RouterOperation(
            path = "${Routes.EXTERNAL_BASE_V1}/p8e/classify/status",
            method = arrayOf(RequestMethod.GET),
            produces = ["application/json"],
            operation = Operation(
                tags = ["Provenance"],
                operationId = "getClassificationStatus",
                method = "POST",
                parameters = [
                    Parameter(
                        name = "x-uuid",
                        required = true,
                        `in` = ParameterIn.HEADER,
                        schema = Schema(implementation = UUID::class),
                    ),
                    Parameter(
                        name = "contractName",
                        required = true,
                        `in` = ParameterIn.QUERY,
                        schema = Schema(implementation = String::class),
                    ),
                    Parameter(
                        name = "assetUuid",
                        required = true,
                        `in` = ParameterIn.QUERY,
                        schema = Schema(implementation = UUID::class),
                    ),
                    Parameter(
                        name = "chainId",
                        required = true,
                        `in` = ParameterIn.QUERY,
                        schema = Schema(implementation = String::class),
                    ),
                    Parameter(
                        name = "nodeEndpoint",
                        required = true,
                        `in` = ParameterIn.QUERY,
                        schema = Schema(implementation = String::class),
                    ),
                ],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        description = "successful operation",
                        content = [Content(schema = Schema(implementation = AssetDefinition::class))]
                    )
                ]
            )
        ),
        RouterOperation(
            path = "${Routes.EXTERNAL_BASE_V1}/p8e/permissions",
            method = arrayOf(RequestMethod.PATCH),
            produces = ["application/json"],
            operation = Operation(
                tags = ["Provenance"],
                operationId = "updateScopeDataAccessRequest",
                method = "PATCH",
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
                    content = [Content(schema = Schema(implementation = UpdateScopeDataAccessRequest::class))]
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
    fun externalProvenanceApiV1(handler: ProvenanceHandler) = coRouter {
        logExchange(log)
        "${Routes.EXTERNAL_BASE_V1}/p8e".nest {
            "/tx".nest {
                POST("/generate", handler::generateTx)
                POST("/execute", handler::executeTx)
            }
            "/classify".nest {
                POST("", handler::classifyAsset)
                GET("/status", handler::getClassificationStatus)
            }
            GET("/scope/query", handler::queryScope)
            POST("/verify", handler::verifyAsset)
            GET("/fees", handler::getFees)
            PATCH("/permissions", handler::updateDataAccess)
        }
    }
}
