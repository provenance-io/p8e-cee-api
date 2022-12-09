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
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.awaitMultipartData
import org.springframework.web.reactive.function.server.queryParamOrNull

private val log = KotlinLogging.logger {}

@Component
class InternalObjectStoreHandler(
    private val storeProto: StoreProto,
    private val storeFile: StoreFile,
    private val getProto: GetProto,
    private val getFile: GetFile,
) {
    suspend fun storeProto(req: ServerRequest): ServerResponse = runCatching {
        storeProto.execute(StoreProtoRequestWrapper(req.getUser(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun storeFile(req: ServerRequest): ServerResponse = runCatching {
        val usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val freeMemory = Runtime.getRuntime().freeMemory()
        val totalMemory = Runtime.getRuntime().totalMemory()
        val maxMemory = Runtime.getRuntime().maxMemory()

        log.info {
            "Storing file with mem usage: \n" +
                "used memory: ${usedMemory / 1000000} MB\n" +
                "free memory: ${freeMemory / 1000000} MB\n" +
                "total memory: ${totalMemory / 1000000} MB\n" +
                "max memory: ${maxMemory / 1000000} MB"
        }
        storeFile.execute(StoreFileRequestWrapper(req.getUser(), req.awaitMultipartData().toSingleValueMap()))
    }.foldToServerResponse()

    suspend fun getProto(req: ServerRequest): ServerResponse = runCatching {
        getProto.execute(GetProtoRequestWrapper(req.getUser(), GetProtoRequest(req.queryParam("hash").get(), req.queryParam("objectStoreAddress").get(), req.queryParam("type").get())))
    }.foldToServerResponse()

    suspend fun getFile(req: ServerRequest): ServerResponse = runCatching {
        getFile.execute(GetFileRequestWrapper(req.getUser(), GetFileRequest(req.queryParam("hash").get(), req.queryParam("objectStoreAddress").get(), rawBytes = req.queryParamOrNull("rawBytes")?.toBoolean() ?: false)))
    }.foldToServerResponse()
}
