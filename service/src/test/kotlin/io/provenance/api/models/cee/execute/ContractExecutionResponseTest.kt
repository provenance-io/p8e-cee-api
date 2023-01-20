package io.provenance.api.models.cee.execute

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.provenance.api.models.p8e.TxResponse
import io.provenance.scope.encryption.domain.inputstream.DIMEInputStream.Companion.configureProvenance
import java.util.UUID

class ContractExecutionResponseTest : FunSpec({
    context("mapping") {
        val mapper = ObjectMapper().configureProvenance()

        test("SinglePartyContractExecutionResponse") {
            val response = SinglePartyContractExecutionResponse(
                metadata = TxResponse(
                    hash = "hash",
                    gasWanted = "2",
                    gasUsed = "3",
                    height = "4"
                ),
                error = "error",
                scopeUuids = listOf(UUID.randomUUID())
            )

            val serialized = mapper.writeValueAsString(response)
            val deserialized: ContractExecutionResponse =
                mapper.readValue(serialized, ContractExecutionResponse::class.java)
            (deserialized as SinglePartyContractExecutionResponse) shouldBe response
        }

        test("ContractExecutionErrorResponse") {
            val response = ContractExecutionErrorResponse(
                errorType = "error type",
                error = "error",
                scopeUuids = listOf(UUID.randomUUID(), UUID.randomUUID())
            )

            val serialized = mapper.writeValueAsString(response)
            val deserialized: ContractExecutionResponse =
                mapper.readValue(serialized, ContractExecutionResponse::class.java)
            (deserialized as ContractExecutionErrorResponse) shouldBe response
        }

        test("MultipartyContractExecutionResponse") {
            val response = MultipartyContractExecutionResponse(
                envelopeState = "envelope",
                error = null,
                scopeUuids = emptyList()
            )

            val serialized = mapper.writeValueAsString(response)
            val deserialized: ContractExecutionResponse =
                mapper.readValue(serialized, ContractExecutionResponse::class.java)
            (deserialized as MultipartyContractExecutionResponse) shouldBe response
        }
    }
})
