package io.provenance.api.domain.usecase.objectStore.get

import com.google.protobuf.BytesValue
import com.google.protobuf.StringValue
import cosmos.bank.v1beta1.QueryOuterClass
import io.provenance.api.domain.extensions.toByteResponse
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.objectStore.get.models.GetFileRequestWrapper
import io.provenance.api.domain.usecase.objectStore.get.models.RetrieveAndDecryptRequest
import io.provenance.client.grpc.GasEstimationMethod
import io.provenance.client.grpc.PbClient
import io.provenance.client.protobuf.extensions.isSet
import java.net.URI
import org.springframework.stereotype.Component
import tech.figure.asset.v1beta1.Asset
import tech.figure.proto.util.FileNFT

@Component
class GetFile(
    private val retrieveAndDecrypt: RetrieveAndDecrypt,
) : AbstractUseCase<GetFileRequestWrapper, Any>() {
    override suspend fun execute(args: GetFileRequestWrapper): Any {
        PbClient("pio-testnet-1-", URI("grpc://34.148.39.82:9090"), GasEstimationMethod.MSG_FEE_CALCULATION).use {
            println(
                it.bankClient.balance(
                    QueryOuterClass.QueryBalanceRequest.newBuilder()
                        .setAddress("tp1lz0kffhs3fggvnqq9t0r40trys90pa880l9q8u")
                        .setDenom("hash")
                        .build()
                )
            )
        }

        retrieveAndDecrypt.execute(RetrieveAndDecryptRequest(args.uuid, args.request.objectStoreAddress, args.request.hash, args.request.accountInfo.keyManagementConfig)).let {
            if (args.request.rawBytes) {
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
