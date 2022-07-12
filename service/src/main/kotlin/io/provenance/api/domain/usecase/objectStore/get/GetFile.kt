package io.provenance.api.domain.usecase.objectStore.get

import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.objectStore.get.models.GetFileRequestWrapper
import io.provenance.api.domain.usecase.objectStore.get.models.RetrieveAndDecryptRequest
import org.springframework.stereotype.Component

@Component
class GetFile(
    private val retrieveAndDecrypt: RetrieveAndDecrypt,
) : AbstractUseCase<GetFileRequestWrapper, ByteArray>() {
    override suspend fun execute(args: GetFileRequestWrapper): ByteArray =
        retrieveAndDecrypt.execute(RetrieveAndDecryptRequest(args.uuid, args.request.objectStoreAddress, args.request.hash, args.request.accountInfo.keyManagementConfig))
}
