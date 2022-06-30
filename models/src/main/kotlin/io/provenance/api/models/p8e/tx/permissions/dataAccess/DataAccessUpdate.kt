package io.provenance.api.models.p8e.tx.permissions.dataAccess

data class DataAccessUpdate(
    val type: DataAccessChangeType,
    val address: String,
)
