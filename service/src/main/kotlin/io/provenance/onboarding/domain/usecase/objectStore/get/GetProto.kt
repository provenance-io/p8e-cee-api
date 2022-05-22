package io.provenance.onboarding.domain.usecase.objectStore.get

import com.google.protobuf.Message
import io.provenance.onboarding.domain.usecase.AbstractUseCase
import io.provenance.onboarding.domain.usecase.objectStore.get.models.GetAssetRequestWrapper
import io.provenance.onboarding.domain.usecase.objectStore.get.models.RetrieveAndDecryptRequest
import io.provenance.onboarding.util.toPrettyJson
import org.springframework.stereotype.Component

@Component
class GetProto(
    private val retrieveAndDecrypt: RetrieveAndDecrypt,
) : AbstractUseCase<GetAssetRequestWrapper, String>() {
    override suspend fun execute(args: GetAssetRequestWrapper): String {
        val message = retrieveAndDecrypt.execute(RetrieveAndDecryptRequest(args.uuid, args.request.objectStoreAddress, args.request.hash.replace(' ', '+')))
        val builder = Class.forName(args.request.type).getMethod("newBuilder").invoke(null) as Message.Builder
        return builder.mergeFrom(message).build().toPrettyJson()
    }
}
