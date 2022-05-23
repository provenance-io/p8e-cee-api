package io.provenance.api.models.account

data class KeyManagementConfig(
    val address: String,
    val tokenPath: String,
    val plugin: String = "io.provenance.plugins.vault.VaultPlugin",
)
