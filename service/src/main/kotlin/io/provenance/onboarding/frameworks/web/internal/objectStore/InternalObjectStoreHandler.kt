package io.provenance.onboarding.frameworks.web.internal.objectStore

import io.provenance.onboarding.domain.usecase.objectStore.StoreAsset
import io.provenance.onboarding.domain.usecase.objectStore.model.StoreAssetRequestWrapper
import io.provenance.onboarding.frameworks.web.misc.foldToServerResponse
import io.provenance.onboarding.frameworks.web.misc.getUser
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody

@Component
class InternalObjectStoreHandler(
    private val storeAsset: StoreAsset
) {
    suspend fun store(req: ServerRequest): ServerResponse = runCatching {
        storeAsset.execute(StoreAssetRequestWrapper(req.getUser(), req.awaitBody()))
    }.foldToServerResponse()
}
