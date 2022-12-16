package io.provenance.api.domain.usecase.objectStore.store

import com.google.gson.Gson
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.common.originator.EntityManager
import io.provenance.api.domain.usecase.common.originator.models.KeyManagementConfigWrapper
import io.provenance.api.domain.usecase.objectStore.store.models.StoreFileRequestWrapper
import io.provenance.api.domain.usecase.objectStore.store.models.StoreObjectRequest
import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.eos.store.StoreProtoResponse
import io.provenance.api.models.p8e.PermissionInfo
import io.provenance.api.util.awaitAllBytes
import java.security.PrivateKey
import java.security.PublicKey
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.codec.multipart.FormFieldPart
import org.springframework.http.codec.multipart.Part
import org.springframework.stereotype.Component
import tech.figure.asset.v1beta1.AssetOuterClassBuilders
import tech.figure.proto.util.FileNFT
import tech.figure.proto.util.toProtoAny

@Component
class StoreFile(
    private val entityManager: EntityManager,
    private val storeObject: StoreObject,
) : AbstractUseCase<StoreFileRequestWrapper, StoreProtoResponse>() {
    override suspend fun execute(args: StoreFileRequestWrapper): StoreProtoResponse {
        val (account, permissions, objectStoreAddress, storeRawBytes, id, file, type) = getParams(args.request)
        val originator = entityManager.getEntity(KeyManagementConfigWrapper(args.uuid.toString(), account?.keyManagementConfig))
        val publicKey = (originator.encryptionPublicKey() as? PublicKey)
            ?: throw IllegalStateException("Public key was not present for originator: ${args.uuid}")

        val privateKey = (originator.encryptionPrivateKey() as? PrivateKey)
            ?: throw IllegalStateException("Private key was not present for originator: ${args.uuid}")

        return file.awaitAllBytes().map { bytes ->
            storeObject.executeBlocking(
                StoreObjectRequest(
                    if (!storeRawBytes)
                        AssetOuterClassBuilders.Asset {
                            idBuilder.value = id
                            this.type = FileNFT.ASSET_TYPE
                            description = file.filename()
                            putKv(FileNFT.KEY_FILENAME, file.filename().toProtoAny())
                            putKv(FileNFT.KEY_BYTES, bytes.toProtoAny())
                            putKv(FileNFT.KEY_SIZE, bytes.size.toString().toProtoAny())
                            putKv(FileNFT.KEY_CONTENT_TYPE, file.headers().contentType.toString().toProtoAny())
                        }.toByteArray() else bytes,
                    type,
                    objectStoreAddress,
                    args.useObjectStoreGateway,
                    publicKey,
                    privateKey,
                    permissions,
                    account ?: AccountInfo()
                )
            )
        }.awaitSingle()
    }

    private fun getParams(request: Map<String, Part>): Args {
        var permissions: PermissionInfo? = null
        var account: AccountInfo? = null
        var type: String? = null

        request["account"]?.let {
            account = Gson().fromJson((it as FormFieldPart).value(), AccountInfo::class.java)
        }

        if (!request.containsKey("id") || request.getAsType<FormFieldPart>("id").value().isEmpty()) {
            throw IllegalArgumentException("Request must provide the 'id' field for the file")
        }

        request["permissions"]?.let {
            permissions = Gson().fromJson((it as FormFieldPart).value(), PermissionInfo::class.java)
        }

        request["type"]?.let {
            type = request.getAsType<FormFieldPart>("type").value()
        }

        val objectStoreAddress = request.getAsType<FormFieldPart>("objectStoreAddress").value()
        val storeRawBytes = request.getAsType<FormFieldPart>("storeRawBytes").value().toBoolean()
        val id = request.getAsType<FormFieldPart>("id").value()
        val file = request.getAsType<FilePart>("file")
        return Args(account, permissions, objectStoreAddress, storeRawBytes, id, file, type)
    }

    private inline fun <reified T> Map<String, Part>.getAsType(key: String): T =
        T::class.java.cast(get(key))
            ?: throw IllegalArgumentException("Failed to retrieve and cast provided argument.")

    data class Args(
        val keyConfig: AccountInfo?,
        val permissions: PermissionInfo?,
        val objectStoreAddress: String,
        val storeRawBytes: Boolean,
        val id: String,
        val file: FilePart,
        val type: String?,
    )
}
