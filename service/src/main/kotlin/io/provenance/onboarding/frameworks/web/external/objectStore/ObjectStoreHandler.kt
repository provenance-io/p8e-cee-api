package io.provenance.onboarding.frameworks.web.external.objectStore

import io.provenance.onboarding.domain.usecase.objectStore.EnableReplication
import io.provenance.onboarding.domain.usecase.objectStore.GetAsset
import io.provenance.onboarding.domain.usecase.objectStore.SnapshotAsset
import io.provenance.onboarding.domain.usecase.objectStore.StoreAsset
import io.provenance.onboarding.frameworks.web.misc.foldToServerResponse
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody

@Component
class ObjectStoreHandler(
    private val storeAsset: StoreAsset,
    private val getAsset: GetAsset,
    private val snapshotAsset: SnapshotAsset,
    private val enableReplication: EnableReplication,
) {
    suspend fun store(req: ServerRequest): ServerResponse = runCatching {
        storeAsset.execute(req.awaitBody())
    }.foldToServerResponse()

    suspend fun snapshot(req: ServerRequest): ServerResponse = runCatching {
        snapshotAsset.execute(req.awaitBody())
    }.foldToServerResponse()

    suspend fun getAsset(req: ServerRequest): ServerResponse = runCatching {
        getAsset.execute(req.awaitBody())
    }.foldToServerResponse()

    suspend fun enableReplication(req: ServerRequest): ServerResponse = runCatching {
        enableReplication.execute(req.awaitBody())
    }.foldToServerResponse()
}
