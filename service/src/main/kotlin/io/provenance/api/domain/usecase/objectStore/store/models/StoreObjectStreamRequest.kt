package io.provenance.api.domain.usecase.objectStore.store.models

import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.p8e.PermissionInfo
import io.provenance.scope.encryption.model.KeyRef
import java.nio.file.Path

/**
 * Request to store the raw contents of a file that has already been spilled to a temporary file on
 * disk, streaming it to the object store instead of loading it into memory.
 *
 * This is the memory-safe counterpart to [StoreObjectRequest] and is only valid for the direct
 * object-store client (never the gateway). The referenced [source] file is owned by the caller.
 */
data class StoreObjectStreamRequest(
    val source: Path,
    val contentLength: Long,
    val type: String?,
    val objectStoreUrl: String,
    val keyRef: KeyRef,
    val permissions: PermissionInfo? = null,
    val account: AccountInfo = AccountInfo(),
)
