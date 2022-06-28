package io.provenance.api.models.p8e.tx.permission

data class DataAccessUpdate(
    val type: DataAccessChangeType,
    val address: String,
)
