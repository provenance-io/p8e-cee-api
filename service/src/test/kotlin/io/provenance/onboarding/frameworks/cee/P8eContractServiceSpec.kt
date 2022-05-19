package io.provenance.onboarding.frameworks.cee

import com.google.protobuf.Message
import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.style.FunSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.provenance.scope.contract.spec.P8eContract
import io.provenance.scope.contract.spec.P8eScopeSpecification
import io.provenance.scope.encryption.util.toJavaPublicKey
import io.provenance.scope.sdk.Client
import io.provenance.scope.sdk.Session
import io.provenance.scope.util.toUuid

val SCOPE_UUID = "11141790-6de2-4d11-b3ad-9a1e16a8b3aa".toUuid()
val SESSION_UUID = "22141790-6de2-4d11-b3ad-9a1e16a8b3bb".toUuid()

/*
There were intermittent failures on the mockk verify line starting with:
      every { mockSessionBuilder.setSessionUuid(SESSION_UUID)

The errors reported was:
    Missing mocked calls inside everyMissing mocked calls inside every { ... } block: make sure the object inside the block is a mock

It appears that separating the mockk<Session.Builder>() and the mockk<Session>() has prevented the intermittent failure.
If this intermittent error resurfaces, we can mark this test as @Ignored and either solve this issue or find an alternate solution.
 */
class P8eContractServiceTest : FunSpec({

    val mockSessionBuilder = mockk<Session.Builder>()
    val p8eContractService = P8eContractService()
    val mockClient = mockk<Client>()
    val mockSession = mockk<Session>()

    beforeTest {
    }

    afterTest {
        clearAllMocks()
    }

    test("setupContract null scope") {

        val audienceSet =
            setOf("0A41042C52EB79307D248B6CFB2A4AF562E403D4826BB0F540F024BBC3937528F6EB0B7FFA7A6585B751DBA25C173E658F3FEAAB0F05980C76E985CE0D55294F3600D7".toJavaPublicKey())
        val records: Map<String, Message> = emptyMap()
        val contract = p8eContractService.getContract(TestP8eContract::class.qualifiedName!!)

        every {
            mockClient.newSession(
                contract,
                TestP8eScopeSpecification()::class.java
            )
        } answers { mockSessionBuilder }
        every { mockSessionBuilder.addDataAccessKeys(audienceSet) } answers { mockSessionBuilder }
        every { mockSessionBuilder.setScopeUuid(SCOPE_UUID) } answers { mockSessionBuilder }
        every { mockSessionBuilder.build() } answers { mockSession }
        every { mockSession.scopeUuid } answers { SCOPE_UUID }
        every { mockSession.sessionUuid } answers { SESSION_UUID }
        every { mockSessionBuilder.setSessionUuid(SESSION_UUID) } answers { mockSessionBuilder }
        p8eContractService.setupContract(
            mockClient,
            contract,
            records,
            SCOPE_UUID,
            SESSION_UUID,
            null,
            null,
            TestP8eScopeSpecification::class.qualifiedName!!,
            audienceSet
        )
        verify(exactly = 1) { mockSessionBuilder.addDataAccessKeys(audienceSet) }
        verify(exactly = 1) { mockSessionBuilder.build() }
    }
})

class TestP8eScopeSpecification : P8eScopeSpecification()

class TestP8eContract : P8eContract()
