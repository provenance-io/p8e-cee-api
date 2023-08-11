package io.provenance.api.domain.usecase.objectStore.get

import com.google.protobuf.Message
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.objectStore.get.models.GetProtoRequestWrapper
import io.provenance.api.domain.usecase.objectStore.get.models.RetrieveAndDecryptRequest
import io.provenance.api.util.toPrettyJson
import org.springframework.stereotype.Component

@Component
class GetProto(
    private val getObject: GetObject,
) : AbstractUseCase<GetProtoRequestWrapper, String>() {
    override suspend fun execute(args: GetProtoRequestWrapper): String {
        val message = getObject.execute(
            RetrieveAndDecryptRequest(
                args.entity,
                args.request.objectStoreAddress,
                args.request.hash,
                args.request.account.keyManagementConfig,
                args.useObjectStoreGateway
            )

        )
        val builder = Class.forName(args.request.type).getMethod("newBuilder").invoke(null) as Message.Builder
        return builder.mergeFrom(message).build().toPrettyJson()
    }
}
