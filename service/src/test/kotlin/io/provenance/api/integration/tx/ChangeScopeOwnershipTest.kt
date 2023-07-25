package io.provenance.api.integration.tx

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.string.shouldContain
import io.provenance.api.domain.usecase.provenance.tx.scope.ChangeScopeOwnership
import io.provenance.api.domain.usecase.provenance.tx.scope.models.ChangeScopeOwnershipRequestWrapper
import io.provenance.api.integration.base.IntegrationTestBase
import io.provenance.api.models.p8e.ProvenanceConfig
import io.provenance.api.models.p8e.tx.ChangeScopeOwnershipRequest
import io.provenance.api.models.user.UserUUID
import io.provenance.scope.util.toUuid
import java.util.UUID

class ChangeScopeOwnershipTest(
    private val changeScopeOwnership: ChangeScopeOwnership,
) : IntegrationTestBase({
    val testEntityId = UserUUID("deadbeef-face-479b-860c-facefaceface".toUuid())
    "Changing scope ownership" should {
        "throw an exception when no changes to the scope are specified" {
            shouldThrow<IllegalArgumentException> {
                changeScopeOwnership.execute(
                    ChangeScopeOwnershipRequestWrapper(
                        testEntityId,
                        ChangeScopeOwnershipRequest(
                            provenanceConfig = ProvenanceConfig(
                                chainId = "chain-local",
                                nodeEndpoint = provenanceAddress,
                            ),
                            scopeId = UUID.randomUUID(),
                            newValueOwner = null,
                            newDataAccess = null,
                        )
                    ).toBatchWrapper()
                )
            }.let { exception ->
                exception.message shouldContain "Must request at least one change to the scope"
            }
        }
        "allow the value owner of an existing scope to be changed" {
            // TODO - Implement
        }
        "allow the data access list of an existing scope to be changed" {
            // TODO - Implement
        }
    }
})
