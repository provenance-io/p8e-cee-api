package com.figure.onboarding.domain.usecase.objectStore.model

data class EnableReplicationRequest(
    val sourceObjectStoreAddress: String,
    val targetObjectStoreAddress: String,
    val targetPublicKey: String,
)
