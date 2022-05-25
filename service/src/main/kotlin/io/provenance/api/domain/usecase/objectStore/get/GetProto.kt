package io.provenance.api.domain.usecase.objectStore.get

import com.google.protobuf.Message
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.objectStore.get.models.GetProtoRequestWrapper
import io.provenance.api.util.toPrettyJson
import io.provenance.api.domain.usecase.objectStore.get.models.RetrieveAndDecryptRequest
import org.springframework.stereotype.Component

@Component
class GetProto(
    private val retrieveAndDecrypt: RetrieveAndDecrypt,
) : AbstractUseCase<GetProtoRequestWrapper, String>() {
    override suspend fun execute(args: GetProtoRequestWrapper): String {
        val message = retrieveAndDecrypt.execute(RetrieveAndDecryptRequest(args.uuid, args.request.objectStoreAddress, args.request.hash, args.request.account.keyManagementConfig))
        val builder = Class.forName(args.request.type).getMethod("newBuilder").invoke(null) as Message.Builder
        return builder.mergeFrom(message).build().toPrettyJson()
    }
}
