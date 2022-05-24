package io.provenance.api.domain.usecase.provenance.tx

import io.kotest.core.spec.style.FunSpec
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.provenance.api.domain.usecase.common.originator.EntityManager
import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.p8e.Audience
import io.provenance.api.models.p8e.AudienceKeyPair
import io.provenance.api.models.p8e.CreateTxRequest
import io.provenance.api.models.p8e.PermissionInfo
import io.provenance.client.grpc.Signer
import io.provenance.core.Originator
import io.provenance.api.domain.usecase.provenance.account.GetSigner
import io.provenance.api.domain.usecase.provenance.tx.model.CreateTxRequestWrapper
import io.provenance.api.frameworks.config.ProvenanceProperties
import io.provenance.scope.encryption.util.toJavaPublicKey
import io.provenance.scope.util.toUuid
import org.junit.jupiter.api.Assertions.assertNotNull
import java.security.PublicKey

const val ADD_ASSET_AUDIENCE_PUBLIC_KEY =
    "0A41046C57E9E25101D5E553AE003E2F79025E389B51495607C796B4E95C0A94001FBC24D84CD0780819612529B803E8AD0A397F474C965D957D33DD64E642B756FBC4"
val CONTRACT_SPEC_ID = "d559982f-a693-4aeb-baf3-15f38c3af213".toUuid()
val SCOPE_SPEC_ID = "c945fa3a-8f78-46f3-a50a-1827ac97d0b9".toUuid()
val SCOPE_ID = "74bd752e-f60f-4ee2-938e-4e88b68663d0".toUuid()
const val IS_TEST_NET = true
const val MOCK_SIGNER_ADDRESS = "mockSignerAddress"
const val DART_AUDIENCE_PUBLIC_KEY_STR =
    "1141046C57E9E25101D5E553AE003E2F79025E389B51495607C796B4E95C0A94001FBC24D84CD0780819612529B803E8AD0A397F474C965D957D33DD64E642B756FB11"
const val PORTFOLIO_MANAGER_AUDIENCE_PUBLICKEY_STR =
    "2241046C57E9E25101D5E553AE003E2F79025E389B51495607C796B4E95C0A94001FBC24D84CD0780819612529B803E8AD0A397F474C965D957D33DD64E642B756FB22"
val ACCOUNT_INFO = AccountInfo()
val REQUEST_UUID = "11141790-6de2-4d11-b3ad-9a1e16a8b3aa".toUuid()

class CreateTxTest : FunSpec({
    val mockEntityManager = mockk<EntityManager>()
    val mockOriginator = mockk<Originator>()
    val mockSigner = mockk<Signer>()
    val mockGetSigner = mockk<GetSigner>()
    val mockOriginatorPublicKey = mockk<PublicKey>()
    val mockProvenanceProperties = mockk<ProvenanceProperties>()

    val createTx = CreateTx(
        mockEntityManager,
        mockGetSigner,
        mockProvenanceProperties,
    )

    beforeTest {
        coEvery { mockGetSigner.execute(any()) } returns mockSigner

        mockkStatic(String::toJavaPublicKey)

        every {
            mockProvenanceProperties.mainnet
        } returns !IS_TEST_NET

        every { mockSigner.address() } returns MOCK_SIGNER_ADDRESS
    }

    afterTest {
        clearAllMocks()
    }

    test("happy path") {
        every { mockOriginator.encryptionPublicKey() } returns mockOriginatorPublicKey
        every { mockEntityManager.hydrateKeys(any()) } returns emptySet()

        // Execute enable replication code
        val response = createTx.execute(
            CreateTxRequestWrapper(
                REQUEST_UUID,
                CreateTxRequest(
                    ACCOUNT_INFO,
                    PermissionInfo(
                        setOf(Audience(null, AudienceKeyPair(ADD_ASSET_AUDIENCE_PUBLIC_KEY, ADD_ASSET_AUDIENCE_PUBLIC_KEY))),
                        permissionDart = true,
                        permissionPortfolioManager = true
                    ),
                    CONTRACT_SPEC_ID,
                    SCOPE_SPEC_ID,
                    SCOPE_ID,
                    ""
                )
            )
        )

        assertNotNull(response.json)
        assertNotNull(response.base64)

        // @TODO Add validation of the contents of the responses here
    }
})
