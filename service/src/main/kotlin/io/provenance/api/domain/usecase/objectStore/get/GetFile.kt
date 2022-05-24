package io.provenance.api.domain.usecase.objectStore.get

import com.google.protobuf.BytesValue
import com.google.protobuf.StringValue
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.client.protobuf.extensions.isSet
import io.provenance.onboarding.domain.extensions.toByteResponse
import io.provenance.onboarding.domain.usecase.objectStore.get.models.GetFileRequestWrapper
import io.provenance.onboarding.domain.usecase.objectStore.get.models.RetrieveAndDecryptRequest
import org.springframework.stereotype.Component
import tech.figure.asset.v1beta1.Asset
import tech.figure.proto.util.FileNFT
import org.springframework.http.HttpEntity

@Component
class GetFile(
    private val retrieveAndDecrypt: RetrieveAndDecrypt,
) : AbstractUseCase<GetFileRequestWrapper, HttpEntity<ByteArray>>() {
    override suspend fun execute(args: GetFileRequestWrapper): HttpEntity<ByteArray> {
        Asset.parseFrom(retrieveAndDecrypt.execute(RetrieveAndDecryptRequest(args.uuid, args.request.objectStoreAddress, args.request.hash, args.request.accountInfo.keyManagementConfig)))
            .takeIf { it.isSet() && it.type == FileNFT.ASSET_TYPE }
            ?.let {
                val fileName = it.getKvOrThrow(FileNFT.KEY_FILENAME).unpack(StringValue::class.java).value
                val contentType = it.getKvOrThrow(FileNFT.KEY_CONTENT_TYPE).unpack(StringValue::class.java).value
                return it.getKvOrThrow(FileNFT.KEY_BYTES).unpack(BytesValue::class.java).value.toByteArray().toByteResponse(fileName, contentType)
            } ?: throw IllegalArgumentException("Provided hash is not an Asset.")
    }
}
