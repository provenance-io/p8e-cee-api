package io.provenance.onboarding.frameworks.web.internal.objectStore

import io.provenance.onboarding.domain.usecase.objectStore.store.StoreProto
import io.provenance.onboarding.domain.usecase.objectStore.store.models.StoreProtoRequestWrapper
import io.provenance.onboarding.frameworks.web.misc.foldToServerResponse
import io.provenance.onboarding.frameworks.web.misc.getUser
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody

@Component
class InternalObjectStoreHandler(
    private val storeAsset: StoreProto
) {
    suspend fun store(req: ServerRequest): ServerResponse = runCatching {
        storeAsset.execute(StoreProtoRequestWrapper(req.getUser(), req.awaitBody()))
    }.foldToServerResponse()
}
