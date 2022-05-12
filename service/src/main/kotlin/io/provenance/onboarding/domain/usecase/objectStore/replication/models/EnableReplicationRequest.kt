package io.provenance.onboarding.domain.usecase.objectStore.replication.models

data class EnableReplicationRequest(
    val sourceObjectStoreAddress: String,
    val targetObjectStoreAddress: String,
    val targetSigningPublicKey: String,
    val targetEncryptionPublicKey: String,
)
