package io.provenance.api.models.eos.store

import io.provenance.objectstore.proto.Objects
import java.util.Base64

data class StoreProtoResponse(
    val hash: String,
    val uri: String,
    val bucket: String,
    val name: String
)

fun Objects.ObjectResponse.toModel() = StoreProtoResponse(Base64.getUrlEncoder().encodeToString(hash.toByteArray()), uri, bucket, name)
