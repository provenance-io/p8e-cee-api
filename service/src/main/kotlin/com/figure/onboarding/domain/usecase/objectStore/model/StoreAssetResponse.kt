package com.figure.onboarding.domain.usecase.objectStore.model

import com.figure.onboarding.frameworks.provenance.extensions.toBase64String
import io.provenance.objectstore.proto.Objects

data class StoreAssetResponse(
    val hash: String,
    val uri: String,
    val bucket: String,
    val name: String
)

fun Objects.ObjectResponse.toModel() = StoreAssetResponse(hash.toByteArray().toBase64String(), uri, bucket, name)
