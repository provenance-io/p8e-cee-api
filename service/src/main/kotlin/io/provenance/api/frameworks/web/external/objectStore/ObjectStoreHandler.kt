package io.provenance.api.frameworks.web.external.objectStore

import io.provenance.api.domain.usecase.objectStore.get.GetFile
import io.provenance.api.domain.usecase.objectStore.get.GetProto
import io.provenance.api.domain.usecase.objectStore.get.models.GetFileRequestWrapper
import io.provenance.api.domain.usecase.objectStore.get.models.GetProtoRequestWrapper
import io.provenance.api.domain.usecase.objectStore.permissions.RegisterObjectAccess
import io.provenance.api.domain.usecase.objectStore.permissions.RegisterScopeObjectsAccess
import io.provenance.api.domain.usecase.objectStore.permissions.RevokeObjectAccess
import io.provenance.api.domain.usecase.objectStore.permissions.model.RegisterObjectAccessRequestWrapper
import io.provenance.api.domain.usecase.objectStore.permissions.model.RegisterScopeObjectsAccessRequestWrapper
import io.provenance.api.domain.usecase.objectStore.permissions.model.RevokeObjectAccessRequestWrapper
import io.provenance.api.domain.usecase.objectStore.replication.EnableReplication
import io.provenance.api.domain.usecase.objectStore.store.StoreFile
import io.provenance.api.domain.usecase.objectStore.store.StoreProto
import io.provenance.api.domain.usecase.objectStore.store.models.StoreFileRequestWrapper
import io.provenance.api.domain.usecase.objectStore.store.models.StoreProtoRequestWrapper
import io.provenance.api.frameworks.web.misc.foldToServerResponse
import io.provenance.api.frameworks.web.misc.getUser
import io.provenance.api.models.eos.get.GetFileRequest
import io.provenance.api.models.eos.get.GetProtoRequest
import io.provenance.api.models.eos.store.toModel
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.awaitMultipartData
import org.springframework.web.reactive.function.server.queryParamOrNull

@Component
@Suppress("TooManyFunctions")
class ObjectStoreHandler(
    private val storeProto: StoreProto,
    private val storeFile: StoreFile,
    private val getProto: GetProto,
    private val getFile: GetFile,
    private val enableReplication: EnableReplication,
    private val registerScopeObjectsAccess: RegisterScopeObjectsAccess,
    private val revokeObjectAccess: RevokeObjectAccess,
    private val registerObjectAccess: RegisterObjectAccess
) {
    suspend fun storeProto(req: ServerRequest): ServerResponse = runCatching {
        storeProto.execute(
            StoreProtoRequestWrapper(
                req.getUser(),
                req.awaitBody()
            )
        )
    }.foldToServerResponse()

    suspend fun storeProtoV2(req: ServerRequest): ServerResponse = runCatching {
        storeProto.execute(
            StoreProtoRequestWrapper(
                req.getUser(),
                req.awaitBody(),
                true
            )
        ).toModel()
    }.foldToServerResponse()

    suspend fun storeFile(req: ServerRequest): ServerResponse = runCatching {
        storeFile.execute(
            StoreFileRequestWrapper(
                req.getUser(),
                req.awaitMultipartData().toSingleValueMap(),
            )
        )
    }.foldToServerResponse()

    suspend fun storeFileV2(req: ServerRequest): ServerResponse = runCatching {
        storeFile.execute(
            StoreFileRequestWrapper(
                req.getUser(),
                req.awaitMultipartData().toSingleValueMap(),
                true
            )
        ).toModel()
    }.foldToServerResponse()

    suspend fun getProto(req: ServerRequest): ServerResponse = runCatching {
        getProto.execute(
            GetProtoRequestWrapper(
                req.getUser(),
                GetProtoRequest(
                    req.queryParam("hash").get(),
                    req.queryParam("objectStoreAddress").get(),
                    req.queryParam("type").get()
                )
            )
        )
    }.foldToServerResponse()

    suspend fun getProtoV2(req: ServerRequest): ServerResponse = runCatching {
        getProto.execute(
            GetProtoRequestWrapper(
                req.getUser(),
                GetProtoRequest(
                    req.queryParam("hash").get(),
                    req.queryParam("objectStoreAddress").get(),
                    req.queryParam("type").get()
                ),
                true
            )
        )
    }.foldToServerResponse()

    suspend fun getFile(req: ServerRequest): ServerResponse = runCatching {
        getFile.execute(
            GetFileRequestWrapper(
                req.getUser(),
                GetFileRequest(
                    req.queryParam("hash").get(),
                    req.queryParam("objectStoreAddress").get(),
                    rawBytes = req.queryParamOrNull("rawBytes")?.toBoolean() ?: false
                ),
            )
        )
    }.foldToServerResponse()

    suspend fun getFileV2(req: ServerRequest): ServerResponse = runCatching {
        getFile.execute(
            GetFileRequestWrapper(
                req.getUser(),
                GetFileRequest(
                    req.queryParam("hash").get(),
                    req.queryParam("objectStoreAddress").get(),
                    rawBytes = req.queryParamOrNull("rawBytes")?.toBoolean() ?: false
                ),
                true
            )
        )
    }.foldToServerResponse()

    suspend fun enableReplication(req: ServerRequest): ServerResponse = runCatching {
        enableReplication.execute(req.awaitBody())
    }.foldToServerResponse()

    suspend fun registerObjectAccess(req: ServerRequest): ServerResponse = runCatching {
        registerObjectAccess.execute(
            RegisterObjectAccessRequestWrapper(
                req.getUser(),
                req.awaitBody()
            )
        )
    }.foldToServerResponse()

    suspend fun revokeObjectAccess(req: ServerRequest): ServerResponse = runCatching {
        revokeObjectAccess.execute(
            RevokeObjectAccessRequestWrapper(
                req.getUser(),
                req.awaitBody()
            )
        )
    }.foldToServerResponse()

    suspend fun registerScopeObjectAccess(req: ServerRequest): ServerResponse = runCatching {
        registerScopeObjectsAccess.execute(
            RegisterScopeObjectsAccessRequestWrapper(
                req.getUser(),
                req.awaitBody()
            )
        )
    }.foldToServerResponse()
}
