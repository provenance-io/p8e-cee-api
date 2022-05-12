package io.provenance.onboarding.frameworks.web.external.objectStore

import io.provenance.onboarding.domain.usecase.objectStore.replication.EnableReplication
import io.provenance.onboarding.domain.usecase.objectStore.get.GetAsset
import io.provenance.onboarding.domain.usecase.objectStore.get.models.GetAssetRequestWrapper
import io.provenance.onboarding.domain.usecase.objectStore.snapshot.SnapshotAsset
import io.provenance.onboarding.domain.usecase.objectStore.snapshot.models.SnapshotAssetRequestWrapper
import io.provenance.onboarding.domain.usecase.objectStore.store.StoreAsset
import io.provenance.onboarding.domain.usecase.objectStore.store.models.StoreAssetRequestWrapper
import io.provenance.onboarding.frameworks.web.misc.foldToServerResponse
import io.provenance.onboarding.frameworks.web.misc.getUser
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
        storeAsset.execute(StoreAssetRequestWrapper(req.getUser(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun snapshot(req: ServerRequest): ServerResponse = runCatching {
        snapshotAsset.execute(SnapshotAssetRequestWrapper(req.getUser(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun getAsset(req: ServerRequest): ServerResponse = runCatching {
        getAsset.execute(GetAssetRequestWrapper(req.getUser(), req.awaitBody()))
    }.foldToServerResponse()

    suspend fun enableReplication(req: ServerRequest): ServerResponse = runCatching {
        enableReplication.execute(req.awaitBody())
    }.foldToServerResponse()
}
