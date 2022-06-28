package io.provenance.api.models.p8e.tx.permissions

data class DataAccessUpdate(
    val type: DataAccessChangeType,
    val address: String,
)
