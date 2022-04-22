package io.provenance.onboarding.frameworks.web.external.cee

import io.provenance.onboarding.domain.usecase.cee.ExecuteContract
import io.provenance.onboarding.frameworks.web.misc.foldToServerResponse
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody

@Component
class CeeHandler(
    private val executeContract: ExecuteContract,
) {
    suspend fun executeContract(req: ServerRequest): ServerResponse = runCatching {
        executeContract.execute(req.awaitBody())
    }.foldToServerResponse()
}
