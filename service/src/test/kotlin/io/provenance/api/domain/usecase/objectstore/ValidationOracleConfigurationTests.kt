package io.provenance.api.domain.usecase.objectstore

import com.google.protobuf.Message
import com.google.protobuf.Timestamp
import cosmos.authz.v1beta1.Authz
import cosmos.authz.v1beta1.Tx
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import io.provenance.api.domain.objectStore.ObjectStore
import io.provenance.api.domain.usecase.common.originator.EntityManager
import io.provenance.api.domain.usecase.objectStore.store.StoreProto
import io.provenance.api.domain.usecase.objectStore.store.models.StoreProtoRequestWrapper
import io.provenance.api.frameworks.cee.parsers.MessageParser
import io.provenance.api.frameworks.config.ObjectStoreConfig
import io.provenance.api.frameworks.provenance.extensions.toAny
import io.provenance.api.frameworks.provenance.extensions.toBase64String
import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.eos.store.StoreProtoRequest
import io.provenance.api.models.eos.store.StoreProtoResponse
import io.provenance.api.models.p8e.Audience
import io.provenance.api.models.p8e.AudienceKeyPair
import io.provenance.api.models.p8e.PermissionInfo
import io.provenance.api.util.toPrettyJson
import io.provenance.core.Originator
import io.provenance.scope.encryption.util.toJavaPrivateKey
import io.provenance.scope.encryption.util.toJavaPublicKey
import io.provenance.scope.objectstore.util.base64Decode
import io.provenance.scope.objectstore.util.base64EncodeString
import io.provenance.scope.util.setValue
import io.provenance.scope.util.toHexString
import io.provenance.scope.util.toUuid
import org.bouncycastle.util.encoders.Hex
import java.security.PublicKey
import org.junit.jupiter.api.Assertions.assertEquals
import tech.figure.asset.v1beta1.Asset
import tech.figure.validationoracle.client.domain.execute.AddValidationDefinitionExecute
import tech.figure.validationoracle.client.domain.model.EntityDetail
import tech.figure.validationoracle.client.domain.model.ValidationCost
import tech.figure.validationoracle.client.domain.model.ValidatorConfiguration
import java.time.Period

const val ADD_ASSET_OBJECT_STORE_ADDRESS = "grpc://localhost:5005"
const val ADD_ASSET_AUDIENCE_PUBLIC_KEY =
    "0A41046C57E9E25101D5E553AE003E2F79025E389B51495607C796B4E95C0A94001FBC24D84CD0780819612529B803E8AD0A397F474C965D957D33DD64E642B756FBC4"
val ASSET_ID = "20141790-6de2-4d11-b3ad-9a1e16a8b38e".toUuid()
const val ASSET = "am9l"
val REQUEST_UUID = "11141790-6de2-4d11-b3ad-9a1e16a8b3aa".toUuid()

class ValidationOracleConfigurationTests : FunSpec({

    val mockObjectStoreConfig = mockk<ObjectStoreConfig>()
    val mockObjectStore = mockk<ObjectStore>()
    val mockEntityManager = mockk<EntityManager>()
    val mockOriginator = mockk<Originator>()
    val mockOriginatorPublicKey = mockk<PublicKey>()
    val mockAddAssetAudiencePublicKey = mockk<PublicKey>()
    val mockParser = mockk<MessageParser>()

    val storeAsset = StoreProto(
        mockObjectStore,
        mockObjectStoreConfig,
        mockEntityManager,
        mockParser,
    )

    beforeTest {
        every { mockObjectStoreConfig.timeoutMs } answers { OBECT_STORE_TIMEOUT_CONFIG }

        coEvery { mockEntityManager.getEntity(any()) } returns mockOriginator

        mockkStatic(String::toJavaPublicKey)

        every {
            ADD_ASSET_AUDIENCE_PUBLIC_KEY.toJavaPublicKey()
        } returns mockAddAssetAudiencePublicKey
    }

    afterTest {
        clearAllMocks()
    }

    test("joe test") {

        val entityDetail = EntityDetail(
            address = "tp1mryqzguyelef5dae7k6l22tnls93cvrc60tjdc",
            name = "Entity1",
            description = "descr2",
            homeUrl = null,
            sourceUrl = null
        )

        val validationCost = ValidationCost(
            amount = 345L.toBigInteger(),
            denom = "nhash",
            feeDestination = entityDetail
        )

        val validatorConfiguration = ValidatorConfiguration(
            validationCosts = listOf(validationCost),
            validationType = "abc123",
            validator = entityDetail
        )

        val avde = AddValidationDefinitionExecute("abc123","displayName123", listOf(validatorConfiguration), true, null)
        println(avde.toPrettyJson())

    }
    test("happy path 3") {
        val hexEncodedPrivateKey = "0A21008A0512DDE5D659AEBB2FD45B2FE8CB7E36B9D639FD7E173D36F7693483956307"
        val privateKeyHexBytes = Hex.decode(hexEncodedPrivateKey)
        val privateKeyBase64EncodedStr = privateKeyHexBytes.base64EncodeString()
        val privateKeyHexBytes2 = privateKeyBase64EncodedStr.base64Decode()
        val hexEncodedPrivateKey2 = privateKeyHexBytes2.toHexString()

        println("hexEncodedPrivateKey: $hexEncodedPrivateKey")
        println("privateKeyHexBytes: ${privateKeyHexBytes.decodeToString()}")
        println("privateKeyBase64EncodedStr: $privateKeyBase64EncodedStr")
        println("privateKeyHexBytes2: ${privateKeyHexBytes2.decodeToString()}")
        println("hexEncodedPrivateKey2: $hexEncodedPrivateKey2")
        println("hexEncodedPrivateKey2.toJavaPrivateKey(): ${hexEncodedPrivateKey2.toJavaPrivateKey()}")

//        val privateKeyBase64EncodedStr2 = "70qTkZA7/iUsskDaZpW8X2gKdKjha+ugA4M9/psYwUc="
//        val privateKeyHexBytes3 = privateKeyBase64EncodedStr2.base64Decode()
//        val hexEncodedPrivateKey3 = privateKeyHexBytes3.toHexString()
//
//        println("hexEncodedPrivateKey3: $hexEncodedPrivateKey3")
//        println("hexEncodedPrivateKey3.toJavaPrivateKey(): ${hexEncodedPrivateKey3.toJavaPrivateKey()}")

    }

    test("happy path 2") {
        println(Tx.MsgGrant.newBuilder()
            .setGranter("tp1hslffrztjp399d86a25fh2khg0c6je3achzhyj")
            .setGrantee("tp196g09ugqd0rhkqgu4cnd97e65akeq9dmhttc25")
            .setGrant(
                Authz.Grant.newBuilder()
                    .setAuthorization(com.google.protobuf.Any.pack(
                        Authz.GenericAuthorization.newBuilder()
                            .setMsg("/provenance.metadata.v1.MsgWriteSessionRequest")
                        .build(), "")
            )
            .setExpiration(Timestamp.newBuilder().setValue(java.time.Instant.now().plus(Period.ofDays(1))).build())
            .build()
        ).build().toAny().toByteArray().toBase64String()
        )

//        println("Asywho/hH6OhsHllK60cBisp4WYw/VCgbus82isVuGrj")

/*
{
  "granter" : "tp1hslffrztjp399d86a25fh2khg0c6je3achzhyj",
  "grantee" : "tp1vrqprzdc89dsj9vxt2xzl9d20sle3a9xuf62vc",
  "grant" : {
    "authorization" : {
      "typeUrl" : "/cosmos.authz.v1beta1.GenericAuthorization",
      "value" : "Ci4vcHJvdmVuYW5jZS5tZXRhZGF0YS52MS5Nc2dXcml0ZVNlc3Npb25SZXF1ZXN0"
    },
    "expiration" : "2022-08-31T20:39:30.717625Z"
  }
}
 */
    }
    test("happy path") {
        val storeAssetResponse = StoreProtoResponse("HASH", "URI", "BUCKET", "NAME")

        every { mockObjectStore.store(any(), any<Message>(), any(), any()) } returns storeAssetResponse
        every { mockEntityManager.hydrateKeys(any<PermissionInfo>()) } returns emptySet()
        every { mockOriginator.encryptionPublicKey() } returns mockOriginatorPublicKey
        every { mockParser.parse(any(), any()) } returns Asset.getDefaultInstance()

        // Execute enable replication code
        val response = storeAsset.execute(
            StoreProtoRequestWrapper(
                REQUEST_UUID,
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

        verify {
            mockObjectStore.store(
                any(),
                any<Message>(),
                mockOriginatorPublicKey,
                any()
            )
        }
    }

    test("exception when public key is not set") {
        every { mockOriginator.encryptionPublicKey() } returns FakeKey()
        every { mockEntityManager.hydrateKeys(any<PermissionInfo>()) } returns emptySet()
        every { mockParser.parse(any(), any()) } returns Asset.getDefaultInstance()

        // Execute enable replication code
        shouldThrow<IllegalStateException> {
            storeAsset.execute(
                StoreProtoRequestWrapper(
                    REQUEST_UUID,
                    StoreProtoRequest(
                        ADD_ASSET_OBJECT_STORE_ADDRESS,
                        PermissionInfo(emptySet()),
                        AccountInfo(),
                        ASSET,
                        String::class.java.canonicalName
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
