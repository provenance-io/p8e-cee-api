package com.figure.onboarding.frameworks.web.external.objectStore

import com.figure.onboarding.domain.usecase.objectStore.EnableReplication
import com.figure.onboarding.domain.usecase.objectStore.GetAsset
import com.figure.onboarding.domain.usecase.objectStore.SnapshotAsset
import com.figure.onboarding.domain.usecase.objectStore.StoreAsset
import com.figure.onboarding.frameworks.web.misc.foldToServerResponse
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
