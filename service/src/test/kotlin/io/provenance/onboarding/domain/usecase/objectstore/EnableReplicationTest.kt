package io.provenance.onboarding.domain.usecase.objectstore

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.provenance.objectstore.proto.PublicKeys
import io.provenance.onboarding.domain.usecase.objectStore.replication.EnableReplication
import io.provenance.onboarding.domain.usecase.objectStore.replication.models.EnableReplicationRequest
import io.provenance.onboarding.frameworks.config.ObjectStoreConfig
import io.provenance.scope.encryption.util.toJavaPublicKey
import io.provenance.scope.objectstore.client.OsClient
import java.security.PublicKey
import java.util.UUID

const val REPLICATION_SOURCE_OBJECT_STORE_ADDRESS = "grpc://localhost:5005"
const val REPLICATION_TARGET_OBJECT_STORE_ADDRESS = "grpc://localhost:5006"
const val REPLICATION_TARGET_PUBLIC_KEY =
    "0A41046C57E9E25101D5E553AE003E2F79025E389B51495607C796B4E95C0A94001FBC24D84CD0780819612529B803E8AD0A397F474C965D957D33DD64E642B756FBC4"
const val OBECT_STORE_TIMEOUT_CONFIG = 2000L

class EnableReplicationTest : FunSpec({

    val createResponseUUID = UUID.randomUUID()
    val mockObjectStoreConfig = mockkClass(ObjectStoreConfig::class)
    val enableReplication = EnableReplication(mockObjectStoreConfig)

    val publicKeyResponse = PublicKeys.PublicKeyResponse
        .newBuilder()
        .setUuid(
            io.provenance.objectstore.proto.Utils.UUID.newBuilder().setValue(createResponseUUID.toString()).build()
        )
        .build()

    beforeTest {
        every { mockObjectStoreConfig.timeoutMs } answers { OBECT_STORE_TIMEOUT_CONFIG }

        //  Add mockk constructor monitor on OsClient for source system replicating, so that we can
        //    verify later that createPublicKey() is called on this instantiated source system OsClient
        mockkConstructor(OsClient::class)

        // Create a mock public key that will be returned on calls to extension call to String.toJavaPublicKey()
        val mockSourcePublicKey = mockk<PublicKey>()
        mockkStatic(String::toJavaPublicKey)
        every {
            REPLICATION_TARGET_PUBLIC_KEY.toJavaPublicKey()
        } returns mockSourcePublicKey
    }

    afterTest {
        clearAllMocks()
    }

    test("happy path") {
        every { anyConstructed<OsClient>().createPublicKey(any(), any(), any()) } returns publicKeyResponse

        // Execute enable replication code
        enableReplication.execute(
            EnableReplicationRequest(
                REPLICATION_SOURCE_OBJECT_STORE_ADDRESS,
                REPLICATION_TARGET_OBJECT_STORE_ADDRESS,
                REPLICATION_TARGET_PUBLIC_KEY,
                REPLICATION_TARGET_PUBLIC_KEY
            )
        )
    }

    test("empty result from createPublicKey call") {
        every {
            anyConstructed<OsClient>().createPublicKey(
                any(),
                any(),
                any()
            )
        } returns null

        shouldThrow<IllegalStateException> {
            enableReplication.execute(
                EnableReplicationRequest(
                    REPLICATION_SOURCE_OBJECT_STORE_ADDRESS,
                    REPLICATION_TARGET_OBJECT_STORE_ADDRESS,
                    REPLICATION_TARGET_PUBLIC_KEY,
                    REPLICATION_TARGET_PUBLIC_KEY,
                )
            )
        }
    }
})
