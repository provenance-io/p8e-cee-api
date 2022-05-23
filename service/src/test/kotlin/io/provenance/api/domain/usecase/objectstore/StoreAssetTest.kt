package io.provenance.api.domain.usecase.objectstore

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.p8e.PermissionInfo
import io.provenance.core.Originator
import io.provenance.api.domain.objectStore.ObjectStore
import io.provenance.api.domain.usecase.common.originator.GetEntity
import io.provenance.api.domain.usecase.objectStore.store.StoreAsset
import io.provenance.api.models.eos.StoreAssetRequest
import io.provenance.api.domain.usecase.objectStore.store.models.StoreAssetRequestWrapper
import io.provenance.api.models.eos.StoreAssetResponse
import io.provenance.api.frameworks.config.ObjectStoreConfig
import io.provenance.api.frameworks.objectStore.AudienceKeyManager
import io.provenance.api.frameworks.objectStore.DefaultAudience
import io.provenance.scope.encryption.util.toJavaPublicKey
import io.provenance.scope.util.toUuid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.security.PublicKey

const val ADD_ASSET_OBJECT_STORE_ADDRESS = "grpc://localhost:5005"
const val ADD_ASSET_AUDIENCE_PUBLIC_KEY =
    "0A41046C57E9E25101D5E553AE003E2F79025E389B51495607C796B4E95C0A94001FBC24D84CD0780819612529B803E8AD0A397F474C965D957D33DD64E642B756FBC4"
val ASSET_ID = "20141790-6de2-4d11-b3ad-9a1e16a8b38e".toUuid()
const val ASSET = "am9l"
val REQUEST_UUID = "11141790-6de2-4d11-b3ad-9a1e16a8b3aa".toUuid()

val ACCOUNT_INFO = AccountInfo()

class StoreAssetTest : FunSpec({

    val mockObjectStoreConfig = mockk<ObjectStoreConfig>()
    val mockObjectStore = mockk<ObjectStore>()
    val mockAudienceKeyManager = mockk<AudienceKeyManager>()
    val mockGetEntity = mockk<GetEntity>()
    val mockOriginator = mockk<Originator>()
    val mockOriginatorPublicKey = mockk<PublicKey>()
    val mockAddAssetAudiencePublicKey = mockk<PublicKey>()
    val mockDartAudiencePublicKey = mockk<PublicKey>()
    val mockPortfolioManagerAudiencePublicKey = mockk<PublicKey>()

    val storeAsset = StoreAsset(
        mockObjectStore,
        mockObjectStoreConfig,
        mockAudienceKeyManager,
        mockGetEntity
    )

    beforeTest {
        every { mockObjectStoreConfig.timeoutMs } answers { OBECT_STORE_TIMEOUT_CONFIG }

        coEvery { mockGetEntity.execute(any()) } returns mockOriginator

        mockkStatic(String::toJavaPublicKey)

        every {
            ADD_ASSET_AUDIENCE_PUBLIC_KEY.toJavaPublicKey()
        } returns mockAddAssetAudiencePublicKey

        every {
            mockAudienceKeyManager.get(DefaultAudience.DART)
        } returns mockDartAudiencePublicKey

        every {
            mockAudienceKeyManager.get(DefaultAudience.PORTFOLIO_MANAGER)
        } returns mockPortfolioManagerAudiencePublicKey
    }

    afterTest {
        clearAllMocks()
    }

    test("happy path") {
        val storeAssetResponse = StoreAssetResponse("HASH", "URI", "BUCKET", "NAME")

        every { mockObjectStore.storeAsset(any(), any(), any(), any()) } returns storeAssetResponse

        every { mockOriginator.encryptionPublicKey() } returns mockOriginatorPublicKey

        // Execute enable replication code
        val response = storeAsset.execute(
            StoreAssetRequestWrapper(
                REQUEST_UUID,
                StoreAssetRequest(
                    ACCOUNT_INFO,
                    ADD_ASSET_OBJECT_STORE_ADDRESS,
                    PermissionInfo(
                        setOf(ADD_ASSET_AUDIENCE_PUBLIC_KEY),
                        permissionDart = true,
                        permissionPortfolioManager = true
                    ),
                    ASSET_ID,
                    ASSET
                )
            )
        )

        assertEquals(response, storeAssetResponse)

        verify {
            mockObjectStore.storeAsset(
                any(),
                withArg {
                    assertEquals(ASSET_ID.toString(), it.id.value)
                },
                mockOriginatorPublicKey,
                withArg {
                    assertEquals(3, it.size)
                    assertTrue(it.contains(mockAddAssetAudiencePublicKey))
                    assertTrue(it.contains(mockPortfolioManagerAudiencePublicKey))
                    assertTrue(it.contains(mockDartAudiencePublicKey))
                }
            )
        }
    }

    test("exception when public key is not set") {
        every { mockOriginator.encryptionPublicKey() } returns FakeKey()

        // Execute enable replication code
        shouldThrow<IllegalStateException> {
            storeAsset.execute(
                StoreAssetRequestWrapper(
                    REQUEST_UUID,
                    StoreAssetRequest(
                        ACCOUNT_INFO,
                        ADD_ASSET_OBJECT_STORE_ADDRESS,
                        PermissionInfo(emptySet()),
                        ASSET_ID,
                        ASSET
                    )
                )
            )
        }
    }
})

class FakeKey : java.security.Key {
    override fun getAlgorithm(): String {
        TODO("Not yet implemented")
    }

    override fun getFormat(): String {
        TODO("Not yet implemented")
    }

    override fun getEncoded(): ByteArray {
        TODO("Not yet implemented")
    }
}
