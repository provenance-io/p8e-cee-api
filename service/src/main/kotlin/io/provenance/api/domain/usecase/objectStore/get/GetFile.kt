package io.provenance.api.domain.usecase.objectStore.get

import com.google.protobuf.BytesValue
import com.google.protobuf.StringValue
import io.provenance.api.domain.extensions.toByteResponse
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.objectStore.get.models.GetFileRequestWrapper
import io.provenance.api.domain.usecase.objectStore.get.models.RetrieveAndDecryptRequest
import io.provenance.client.protobuf.extensions.isSet
import org.springframework.stereotype.Component
import tech.figure.asset.v1beta1.Asset
import tech.figure.proto.util.FileNFT

@Component
class GetFile(
    private val retrieveAndDecrypt: RetrieveAndDecrypt,
) : AbstractUseCase<GetFileRequestWrapper, Any>() {
    override suspend fun execute(args: GetFileRequestWrapper): Any {
        retrieveAndDecrypt.execute(RetrieveAndDecryptRequest(args.uuid, args.request.objectStoreAddress, args.request.hash, args.request.accountInfo.keyManagementConfig)).let {
            if (args.rawBytes) {
                return it
            } else {
                Asset.parseFrom(it)
                    .takeIf { asset -> asset.isSet() && asset.type == FileNFT.ASSET_TYPE }
                    ?.let { asset ->
                        val fileName = asset.getKvOrThrow(FileNFT.KEY_FILENAME).unpack(StringValue::class.java).value
                        val contentType = asset.getKvOrThrow(FileNFT.KEY_CONTENT_TYPE).unpack(StringValue::class.java).value
                        return asset.getKvOrThrow(FileNFT.KEY_BYTES).unpack(BytesValue::class.java).value.toByteArray().toByteResponse(fileName, contentType)
                    } ?: throw IllegalArgumentException("Provided hash is not an Asset.")
            }
        }
    }
}
