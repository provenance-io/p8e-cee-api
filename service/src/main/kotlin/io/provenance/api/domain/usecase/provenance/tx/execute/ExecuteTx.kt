package io.provenance.api.domain.usecase.provenance.tx.execute

import com.google.protobuf.Any
import io.provenance.api.domain.provenance.Provenance
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.account.models.GetSignerRequest
import io.provenance.api.domain.usecase.provenance.tx.execute.models.ExecuteTxRequestWrapper
import io.provenance.api.frameworks.provenance.extensions.toTxResponse
import io.provenance.api.models.p8e.ProvenanceConfig
import io.provenance.api.models.p8e.TxResponse
import java.util.Base64
import org.springframework.stereotype.Component

@Component
class ExecuteTx(
    private val provenance: Provenance,
    private val getSigner: GetSigner,
) : AbstractUseCase<ExecuteTxRequestWrapper, TxResponse>() {
    override suspend fun execute(args: ExecuteTxRequestWrapper): TxResponse {
        val signer = getSigner.execute(GetSignerRequest(args.entity, args.request.account))
        val messages = args.request.tx.base64.map { tx ->
            Any.parseFrom(Base64.getDecoder().decode(tx))
        }

        return provenance.executeTransaction(ProvenanceConfig(args.request.chainId, args.request.nodeEndpoint), messages, signer).toTxResponse()
    }
}
