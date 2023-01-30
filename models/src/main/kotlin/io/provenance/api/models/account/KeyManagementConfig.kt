package io.provenance.api.models.account

import io.provenance.core.PluginConfig

data class KeyManagementConfig(
    val plugin: String = "io.provenance.plugins.vault.VaultPlugin",
    val pluginConfig: PluginConfig
)
