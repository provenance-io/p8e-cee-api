package io.provenance.api.frameworks.web.internal.objectStore

import io.provenance.api.domain.usecase.objectStore.get.GetFile
import io.provenance.api.domain.usecase.objectStore.get.GetProto
import io.provenance.api.domain.usecase.objectStore.get.models.GetFileRequestWrapper
import io.provenance.api.domain.usecase.objectStore.get.models.GetProtoRequestWrapper
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
class InternalObjectStoreHandler(
    private val storeProto: StoreProto,
    private val storeFile: StoreFile,
    private val getProto: GetProto,
    private val getFile: GetFile,
) {
    suspend fun storeProto(req: ServerRequest): ServerResponse = runCatching {
        storeProto.execute(
            StoreProtoRequestWrapper(
                req.getUser(),
                req.awaitBody()
            )
        )
    }.foldToServerResponse()

    suspend fun storeFile(req: ServerRequest): ServerResponse = runCatching {
        storeFile.execute(
            StoreFileRequestWrapper(
                req.getUser(),
                req.awaitMultipartData().toSingleValueMap()
            )
        )
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

    suspend fun getFile(req: ServerRequest): ServerResponse = runCatching {
        getFile.execute(
            GetFileRequestWrapper(
                req.getUser(),
                GetFileRequest(
                    req.queryParam("hash").get(),
                    req.queryParam("objectStoreAddress").get(),
                    rawBytes = req.queryParamOrNull("rawBytes")?.toBoolean() ?: false
                )
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

    suspend fun storeFileV2(req: ServerRequest): ServerResponse = runCatching {
        storeFile.execute(
            StoreFileRequestWrapper(
                req.getUser(),
                req.awaitMultipartData().toSingleValueMap(),
                true
            )
        ).toModel()
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
}
