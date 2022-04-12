package io.provenance.onboarding.domain.usecase.objectStore.model

data class EnableReplicationRequest(
    val sourceObjectStoreAddress: String,
    val targetObjectStoreAddress: String,
    val targetPublicKey: String,
)
