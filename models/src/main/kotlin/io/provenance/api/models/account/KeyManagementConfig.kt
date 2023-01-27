package io.provenance.api.models.account

data class KeyManagementConfig(
    val plugin: String = "io.provenance.plugins.vault.VaultPlugin",
    val pluginSpec: Any
)
