package io.provenance.api.models.p8e

import cosmos.tx.v1beta1.ServiceOuterClass

data class ProvenanceConfig(
    val chainId: String,
    val nodeEndpoint: String,
    val gasAdjustment: Double? = 1.5,
    val broadcastMode: ServiceOuterClass.BroadcastMode = ServiceOuterClass.BroadcastMode.BROADCAST_MODE_BLOCK,
    val feeGranter: String? = null,
    val timeoutHeight: Long? = null,
)
