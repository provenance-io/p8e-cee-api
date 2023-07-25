package io.provenance.api.domain.usecase.objectstore

import io.kotest.core.spec.style.FunSpec
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.provenance.api.domain.objectStore.ObjectStore
import io.provenance.api.domain.usecase.common.originator.EntityManager
import io.provenance.api.domain.usecase.objectStore.store.StoreObject
import io.provenance.api.domain.usecase.objectStore.store.StoreProto
import io.provenance.api.domain.usecase.objectStore.store.models.StoreProtoRequestWrapper
import io.provenance.api.frameworks.cee.parsers.MessageParser
import io.provenance.api.frameworks.config.ObjectStoreProperties
import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.eos.store.StoreProtoRequest
import io.provenance.api.models.eos.store.StoreProtoResponse
import io.provenance.api.models.p8e.Audience
import io.provenance.api.models.p8e.AudienceKeyPair
import io.provenance.api.models.p8e.PermissionInfo
import io.provenance.api.models.user.UserUUID
import io.provenance.entity.KeyEntity
import io.provenance.scope.encryption.model.DirectKeyRef
import io.provenance.scope.encryption.util.toJavaPublicKey
import io.provenance.scope.objectstore.client.OsClient
import io.provenance.scope.util.toUuid
import java.security.PrivateKey
import java.security.PublicKey
import org.junit.jupiter.api.Assertions.assertEquals
import tech.figure.asset.v1beta1.Asset

const val ADD_ASSET_OBJECT_STORE_ADDRESS = "grpc://localhost:5005"
const val ADD_ASSET_AUDIENCE_PUBLIC_KEY =
    "0A41046C57E9E25101D5E553AE003E2F79025E389B51495607C796B4E95C0A94001FBC24D84CD0780819612529B803E8AD0A397F474C965D957D33DD64E642B756FBC4"
val ASSET_ID = "20141790-6de2-4d11-b3ad-9a1e16a8b38e".toUuid()
const val ASSET = "am9l"
val REQUEST_UUID = "11141790-6de2-4d11-b3ad-9a1e16a8b3aa".toUuid()

class StoreAssetTest : FunSpec({

    val mockObjectStoreProperties = mockk<ObjectStoreProperties>()
    val mockObjectStore = mockk<ObjectStore>()
    val mockStoreObject = mockk<StoreObject>(relaxed = true)
    val mockEntityManager = mockk<EntityManager>()
    val mockOriginator = mockk<KeyEntity>()
    val mockOriginatorPublicKey = mockk<PublicKey>()
    val mockOriginatorPrivateKey = mockk<PrivateKey>()
    val mockAddAssetAudiencePublicKey = mockk<PublicKey>()
    val mockParser = mockk<MessageParser>()

    val storeAsset = StoreProto(
        mockParser,
        mockStoreObject,
        mockEntityManager,
    )

    beforeTest {
        every { mockObjectStoreProperties.timeoutMs } answers { OBECT_STORE_TIMEOUT_CONFIG }

        coEvery { mockEntityManager.getEntity(any()) } returns mockOriginator

        mockkStatic(String::toJavaPublicKey)

        every {
            ADD_ASSET_AUDIENCE_PUBLIC_KEY.toJavaPublicKey()
        } returns mockAddAssetAudiencePublicKey
    }

    afterTest {
        clearAllMocks()
    }

    test("happy path") {
        val storeAssetResponse = StoreProtoResponse("HASH", "URI", "BUCKET", "NAME")

        every { mockObjectStore.store(any<OsClient>(), any(), any(), any(), any()) } returns storeAssetResponse
        coEvery { mockStoreObject.execute(any()) } returns storeAssetResponse
        every { mockEntityManager.hydrateKeys(any<PermissionInfo>()) } returns emptySet()
        every { mockOriginator.publicKey(any()) } returns mockOriginatorPublicKey
        every { mockOriginator.getKeyRef(any()) } returns DirectKeyRef(mockOriginatorPublicKey, mockOriginatorPrivateKey)
        every { mockParser.parse(any(), any()) } returns Asset.getDefaultInstance()

        // Execute enable replication code
        val response = storeAsset.execute(
            StoreProtoRequestWrapper(
                UserUUID(REQUEST_UUID),
                StoreProtoRequest(
                    ADD_ASSET_OBJECT_STORE_ADDRESS,
                    PermissionInfo(
                        setOf(Audience(null, AudienceKeyPair(ADD_ASSET_AUDIENCE_PUBLIC_KEY, ADD_ASSET_AUDIENCE_PUBLIC_KEY))),
                        permissionDart = true,
                        permissionPortfolioManager = true
                    ),
                    AccountInfo(),
                    ASSET,
                    String::class.java.canonicalName
                )
            )
        )

        assertEquals(response, storeAssetResponse)

        coVerify {
            mockStoreObject.execute(
                any()
            )
        }
    }
})

class FakeKey : PublicKey {
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
