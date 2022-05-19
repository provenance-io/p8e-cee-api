package io.provenance.onboarding.frameworks.web.external.objectStore

import io.provenance.api.models.eos.GetProtoRequest
import io.provenance.api.models.eos.SnapshotAssetRequest
import io.provenance.onboarding.domain.usecase.objectStore.replication.models.EnableReplicationRequest
import io.provenance.api.models.eos.StoreProtoRequest
import io.provenance.api.models.eos.StoreAssetResponse
import io.provenance.onboarding.frameworks.web.Routes
import io.provenance.onboarding.frameworks.web.logging.logExchange
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import mu.KotlinLogging
import org.springdoc.core.annotations.RouterOperation
import org.springdoc.core.annotations.RouterOperations
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.reactive.function.server.coRouter
import java.util.UUID

private val log = KotlinLogging.logger {}

@Configuration
class ObjectStoreApi {
    @Bean
    @RouterOperations(
        RouterOperation(
            path = "${Routes.EXTERNAL_BASE_V1}/eos",
            method = arrayOf(RequestMethod.POST),
            produces = ["application/json"],
            operation = Operation(
                tags = ["Object Store"],
                operationId = "postObjectToStore",
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
                    content = [Content(schema = Schema(implementation = StoreProtoRequest::class))]
                ),
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        description = "successful operation",
                        content = [Content(schema = Schema(implementation = StoreAssetResponse::class))]
                    )
                ]
            )
        ),
        RouterOperation(
            path = "${Routes.EXTERNAL_BASE_V1}/eos/file",
            method = arrayOf(RequestMethod.POST),
            produces = ["application/json"],
            operation = Operation(
                tags = ["Object Store"],
                operationId = "snapshotObjectStoreHash",
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
                    content = [Content(schema = Schema(implementation = SnapshotAssetRequest::class))]
                ),
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        description = "successful operation",
                        content = [Content(schema = Schema(implementation = StoreAssetResponse::class))]
                    )
                ]
            )
        ),
        RouterOperation(
            path = "${Routes.EXTERNAL_BASE_V1}/eos",
            method = arrayOf(RequestMethod.POST),
            produces = ["application/json"],
            operation = Operation(
                tags = ["Object Store"],
                operationId = "postObjectToStore",
                method = "GET",
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
                    content = [Content(schema = Schema(implementation = GetProtoRequest::class))]
                ),
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        description = "successful operation",
                        content = [Content(schema = Schema(implementation = String::class))]
                    )
                ]
            )
        ),
        RouterOperation(
            path = "${Routes.EXTERNAL_BASE_V1}/config/replication/enable",
            method = arrayOf(RequestMethod.POST),
            produces = ["application/json"],
            operation = Operation(
                tags = ["Object Store"],
                operationId = "enableReplication",
                method = "GET",
                requestBody = RequestBody(
                    required = true,
                    content = [Content(schema = Schema(implementation = EnableReplicationRequest::class))]
                ),
                responses = [
                    ApiResponse(
                        responseCode = "204",
                        description = "successful operation",
                    )
                ]
            )
        )
    )
    fun externalObjectStoreApiV1(handler: ObjectStoreHandler) = coRouter {
        logExchange(log)
        Routes.EXTERNAL_BASE_V1.nest {
            "/eos".nest {
                POST("/file", handler::storeFile)
                POST("", handler::storeProto)
                GET("/file", handler::getFile)
                GET("", handler::getProto)
            }
            POST("/config/replication/enable", handler::enableReplication)
        }
    }
}
