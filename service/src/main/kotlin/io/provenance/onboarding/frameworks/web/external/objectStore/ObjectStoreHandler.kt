package io.provenance.onboarding.frameworks.web.external.objectStore

import io.provenance.api.models.eos.GetFileRequest
import io.provenance.onboarding.domain.usecase.objectStore.get.GetFile
import io.provenance.onboarding.domain.usecase.objectStore.replication.EnableReplication
import io.provenance.onboarding.domain.usecase.objectStore.get.GetProto
import io.provenance.onboarding.domain.usecase.objectStore.get.models.GetAssetRequestWrapper
import io.provenance.onboarding.domain.usecase.objectStore.get.models.GetFileRequestWrapper
import io.provenance.onboarding.domain.usecase.objectStore.store.StoreFile
import io.provenance.onboarding.domain.usecase.objectStore.store.StoreProto
import io.provenance.onboarding.domain.usecase.objectStore.store.models.StoreProtoRequestWrapper
import io.provenance.onboarding.domain.usecase.objectStore.store.models.StoreFileRequestWrapper
import io.provenance.onboarding.frameworks.web.misc.foldToServerResponse
import io.provenance.onboarding.frameworks.web.misc.getUser
import io.provenance.scope.util.toUuid
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.awaitMultipartData

@Component
class ObjectStoreHandler(
    private val storeProto: StoreProto,
    private val storeFile: StoreFile,
    private val getProto: GetProto,
    private val getFile: GetFile,
    private val enableReplication: EnableReplication,
) {
    suspend fun storeProto(req: ServerRequest): ServerResponse = runCatching {
        storeProto.execute(StoreProtoRequestWrapper(req.getUser(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun storeFile(req: ServerRequest): ServerResponse = runCatching {
        storeFile.execute(StoreFileRequestWrapper(req.getUser(), req.awaitMultipartData().toSingleValueMap()))
    }.foldToServerResponse()

    suspend fun getProto(req: ServerRequest): ServerResponse = runCatching {
        getProto.execute(GetAssetRequestWrapper(req.getUser(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun getFile(req: ServerRequest): ServerResponse = runCatching {
        getFile.execute(GetFileRequestWrapper(req.getUser(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun enableReplication(req: ServerRequest): ServerResponse = runCatching {
        enableReplication.execute(req.awaitBody())
    }.foldToServerResponse()
}
