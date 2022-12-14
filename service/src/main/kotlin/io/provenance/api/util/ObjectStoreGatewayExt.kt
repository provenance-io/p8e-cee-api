package io.provenance.api.util

import io.provenance.api.models.eos.store.StoreProtoResponse
import java.util.Base64
import tech.figure.objectstore.gateway.GatewayOuterClass

fun GatewayOuterClass.PutObjectResponse.toModel() = StoreProtoResponse(Base64.getUrlEncoder().encodeToString(hash.toByteArray()), "", "", "")
